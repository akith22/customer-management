package com.customer.backend.service;

import com.customer.backend.dto.response.BulkUploadResultDTO;
import com.customer.backend.model.Customer;
import com.customer.backend.repository.CustomerRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Handles bulk customer creation and update from an Excel file.
 *
 * Design decisions:
 *
 *  1. CHUNK-BASED PROCESSING — rows are processed in batches of CHUNK_SIZE.
 *     Each chunk is flushed in its own transaction via self-injection (see
 *     note below). A bad chunk rolls back independently; the rest proceed.
 *     This prevents OOM on 1M-row files and keeps individual transactions
 *     short.
 *
 *  2. SELF-INJECTION for @Transactional on flushChunk — Spring's AOP proxy
 *     only intercepts calls that come in from *outside* the bean. A direct
 *     this.flushChunk() call bypasses the proxy and the @Transactional
 *     annotation is silently ignored. Injecting 'self' via @Lazy resolves
 *     the circular dependency and routes the call through the proxy.
 *
 *  3. CREATE vs UPDATE — if a NIC already exists in the database the row is
 *     treated as an update (name + DOB are overwritten). The original code
 *     skipped duplicates entirely, which violated the assignment spec.
 *
 *  4. SINGLE NIC-EXISTENCE CHECK PER CHUNK — instead of one SELECT per row
 *     we fire one IN-query for the entire chunk's NIC list and then separate
 *     the batch into inserts vs updates in memory.
 */
@Service
public class BulkUploadService {

    // Rows per transaction. 500 is a safe default: low enough to avoid
    // locking contention on MariaDB, high enough to keep overhead low.
    private static final int CHUNK_SIZE = 500;

    // Apache POI default byte-array cap is 100 MB. Raise it to cover the
    // 200 MB multipart limit configured in application.properties.
    static {
        IOUtils.setByteArrayMaxOverride(250_000_000);
    }

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CustomerRepository customerRepository;

    /**
     * Self-reference injected lazily to allow @Transactional to work on
     * flushChunk when called from within the same bean.
     * @Lazy breaks the circular dependency that would otherwise prevent
     * application startup.
     */
    @Autowired
    @Lazy
    private BulkUploadService self;

    public BulkUploadService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ── PUBLIC ENTRY POINT ───────────────────────────────────────────

    public BulkUploadResultDTO processExcelUpload(MultipartFile file) {

        BulkUploadResultDTO result = new BulkUploadResultDTO();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum(); // 0-indexed; row 0 is header
            result.setTotalRows(lastRow);        // data rows = lastRowNum

            List<Customer> batch    = new ArrayList<>(CHUNK_SIZE);
            Set<String>    batchNics = new LinkedHashSet<>(CHUNK_SIZE);

            for (int i = 1; i <= lastRow; i++) {

                Row row = sheet.getRow(i);
                if (row == null) {
                    // Entirely empty row — skip silently, don't count as failure
                    continue;
                }

                try {
                    Customer candidate = parseRow(row, i);
                    String   nic       = candidate.getNic();

                    // Deduplicate within the same file batch: keep the LAST
                    // occurrence of a NIC so a file can effectively "update"
                    // a row it defined earlier in the same upload.
                    if (batchNics.contains(nic)) {
                        // Remove the earlier entry and replace with this one
                        batch.removeIf(c -> c.getNic().equals(nic));
                    }

                    batchNics.add(nic);
                    batch.add(candidate);

                } catch (IllegalArgumentException e) {
                    result.incrementFailed();
                    result.addError("Row " + (i + 1) + ": " + e.getMessage());
                }

                if (batch.size() >= CHUNK_SIZE) {
                    // Route through self proxy so @Transactional is honoured
                    self.flushChunk(batch, result);
                    batch.clear();
                    batchNics.clear();
                }
            }

            // Flush the final partial batch
            if (!batch.isEmpty()) {
                self.flushChunk(batch, result);
            }

        } catch (Exception e) {
            result.addError("Fatal error reading file: " + e.getMessage());
        }

        return result;
    }

    // ── CHUNK FLUSH (transactional) ──────────────────────────────────

    /**
     * Persists one chunk. Must be called via the Spring proxy (i.e. through
     * {@code self.flushChunk(...)}) so that @Transactional takes effect.
     *
     * Strategy:
     *  - One IN-query to find which NICs already exist → update those.
     *  - Everything else → insert.
     *  - One saveAll call for the entire chunk.
     */
    @Transactional
    public void flushChunk(List<Customer> batch, BulkUploadResultDTO result) {

        // Collect NICs in this chunk
        List<String> chunkNics = new ArrayList<>(batch.size());
        for (Customer c : batch) chunkNics.add(c.getNic());

        // Single DB round-trip: which of these NICs already exist?
        List<Customer> existingCustomers =
                customerRepository.findAllByNicIn(chunkNics);

        // Build a map for O(1) lookup: nic → existing Customer entity
        Map<String, Customer> existingByNic = new HashMap<>(existingCustomers.size());
        for (Customer existing : existingCustomers) {
            existingByNic.put(existing.getNic(), existing);
        }

        List<Customer> toSave = new ArrayList<>(batch.size());

        for (Customer candidate : batch) {
            Customer existing = existingByNic.get(candidate.getNic());

            if (existing != null) {
                // UPDATE — overwrite mutable fields, keep ID and audit dates
                existing.setName(candidate.getName());
                existing.setDob(candidate.getDob());
                toSave.add(existing);
                result.incrementUpdated();
            } else {
                // INSERT — brand-new customer
                toSave.add(candidate);
                result.incrementSuccess();
            }
        }

        if (!toSave.isEmpty()) {
            customerRepository.saveAll(toSave);
        }
    }

    // ── ROW PARSER ───────────────────────────────────────────────────

    /**
     * Parses a single Excel row into a transient Customer entity.
     *
     * Expected column order (0-indexed):
     *   A(0) = Name | B(1) = Date of Birth (yyyy-MM-dd) | C(2) = NIC
     *
     * Only mandatory fields are in scope for bulk upload. Optional fields
     * (mobiles, addresses, family members) must be managed via the
     * single-customer form — adding them to the Excel spec would make the
     * template impossibly complex and is outside the assignment scope.
     *
     * @throws IllegalArgumentException with a user-readable message if any
     *         mandatory field is missing or malformed.
     */
    private Customer parseRow(Row row, int rowIndex) {

        String name   = getCellString(row, 0);
        String dobRaw = getCellString(row, 1);
        String nic    = getCellString(row, 2);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (dobRaw == null || dobRaw.trim().isEmpty()) {
            throw new IllegalArgumentException("Date of Birth is required.");
        }
        if (nic == null || nic.trim().isEmpty()) {
            throw new IllegalArgumentException("NIC is required.");
        }

        // Validate NIC format: 12 digits OR 9 digits followed by V/v
        String trimmedNic = nic.trim();
        if (!trimmedNic.matches("^\\d{12}$") && !trimmedNic.matches("^\\d{9}[Vv]$")) {
            throw new IllegalArgumentException(
                    "Invalid NIC format '" + trimmedNic +
                            "'. Must be 12 digits or 9 digits followed by 'V'.");
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobRaw.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format '" + dobRaw +
                            "'. Expected yyyy-MM-dd (e.g. 1990-07-25).");
        }

        if (dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Date of birth '" + dobRaw + "' cannot be in the future.");
        }

        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setDob(dob);
        customer.setNic(trimmedNic);

        return customer;
    }

    // ── CELL VALUE HELPER ────────────────────────────────────────────

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                // Dates stored as Excel numeric cells (e.g. formatted columns)
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue()
                            .toLocalDate()
                            .format(DATE_FMT);
                }
                // NICs stored as a plain number — cast to long to avoid
                // scientific notation from getNumericCellValue()
                return String.valueOf((long) cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                // Evaluate formula result rather than returning the formula string
                FormulaEvaluator evaluator = cell.getSheet()
                        .getWorkbook()
                        .getCreationHelper()
                        .createFormulaEvaluator();
                CellValue evaluated = evaluator.evaluate(cell);
                switch (evaluated.getCellType()) {
                    case STRING:  return evaluated.getStringValue();
                    case NUMERIC: return String.valueOf((long) evaluated.getNumberValue());
                    default:      return null;
                }

            default:
                return null;
        }
    }
}