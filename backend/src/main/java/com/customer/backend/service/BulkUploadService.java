package com.customer.backend.service;

import com.customer.backend.dto.response.BulkUploadResultDTO;
import com.customer.backend.model.Customer;
import com.customer.backend.repository.CustomerRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BulkUploadService {

    /*
     *  Chunk size controls how many rows are held in memory and saved
     *  per transaction. Keeping this at 500–1000 prevents OOM errors
     *  on 1,000,000 row files while still being fast.
     */
    private static final int CHUNK_SIZE = 500;

    // Apache POI default is 100MB — raise it to cover large uploads
    static {
        IOUtils.setByteArrayMaxOverride(250_000_000);
    }

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CustomerRepository customerRepository;

    public BulkUploadService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ── MAIN ENTRY POINT ─────────────────────────────────────────────

    public BulkUploadResultDTO processExcelUpload(MultipartFile file) {

        BulkUploadResultDTO result = new BulkUploadResultDTO();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            result.setTotalRows(lastRow); // row 0 is header

            List<Customer> batch = new ArrayList<>(CHUNK_SIZE);
            Set<String> batchNics = new HashSet<>();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    result.setSkippedCount(result.getSkippedCount() + 1);
                    continue;
                }

                try {
                    Customer customer = parseRow(row, i);

                    // Skip duplicate NIC within the same batch
                    if (batchNics.contains(customer.getNic())) {
                        result.setSkippedCount(result.getSkippedCount() + 1);
                        result.addError("Row " + (i + 1) + ": Duplicate NIC in file — "
                                + customer.getNic() + " (skipped)");
                        continue;
                    }

                    batchNics.add(customer.getNic());
                    batch.add(customer);

                } catch (IllegalArgumentException e) {
                    result.setFailedCount(result.getFailedCount() + 1);
                    result.addError("Row " + (i + 1) + ": " + e.getMessage());
                }

                // Flush chunk to DB when batch is full
                if (batch.size() >= CHUNK_SIZE) {
                    int saved = flushChunk(batch, batchNics, result);
                    result.setSuccessCount(result.getSuccessCount() + saved);
                    batch.clear();
                    batchNics.clear();
                }
            }

            // Flush remaining rows
            if (!batch.isEmpty()) {
                int saved = flushChunk(batch, batchNics, result);
                result.setSuccessCount(result.getSuccessCount() + saved);
            }

        } catch (Exception e) {
            result.addError("Fatal error reading file: " + e.getMessage());
        }

        return result;
    }

    // ── CHUNK FLUSH ──────────────────────────────────────────────────

    /*
     *  Each chunk flush is its own transaction. This means a bad chunk
     *  only rolls back that chunk — not the entire million-row upload.
     *  We also do a single batch NIC existence check per chunk instead
     *  of one SELECT per row.
     */
    @Transactional
    public int flushChunk(List<Customer> batch,
                          Set<String> batchNics,
                          BulkUploadResultDTO result) {

        List<String> nicList = new ArrayList<>(batchNics);

        // Single DB call to find all already-existing NICs in this chunk
        List<String> existingNics = customerRepository.findExistingNics(nicList);
        Set<String> existingNicSet = new HashSet<>(existingNics);

        List<Customer> toSave = new ArrayList<>();

        for (Customer c : batch) {
            if (existingNicSet.contains(c.getNic())) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                result.addError("Skipped duplicate NIC (already in DB): " + c.getNic());
            } else {
                toSave.add(c);
            }
        }

        if (!toSave.isEmpty()) {
            customerRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    // ── ROW PARSER ───────────────────────────────────────────────────

    /*
     *  Expected Excel column order:
     *  A(0) = Name | B(1) = Date of Birth (yyyy-MM-dd) | C(2) = NIC
     *
     *  Only mandatory fields are handled in bulk upload as per spec.
     *  Optional fields (mobiles, addresses, family) are not in scope
     *  for bulk — users manage those through the single-customer form.
     */
    private Customer parseRow(Row row, int rowIndex) {

        String name = getCellString(row, 0);
        String dobRaw = getCellString(row, 1);
        String nic = getCellString(row, 2);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (dobRaw == null || dobRaw.trim().isEmpty()) {
            throw new IllegalArgumentException("Date of Birth is required.");
        }
        if (nic == null || nic.trim().isEmpty()) {
            throw new IllegalArgumentException("NIC is required.");
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobRaw.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format '" + dobRaw + "'. Expected yyyy-MM-dd.");
        }

        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setDob(dob);
        customer.setNic(nic.trim());

        return customer;
    }

    // ── CELL VALUE HELPER ────────────────────────────────────────────

    private String getCellString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue()
                            .toLocalDate()
                            .format(DATE_FORMATTER);
                }
                // Handle NIC stored as a number in Excel
                long numericVal = (long) cell.getNumericCellValue();
                return String.valueOf(numericVal);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}