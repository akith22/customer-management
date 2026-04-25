package com.customer.backend.service;


import com.customer.backend.dto.response.BulkUploadResultDTO;
import com.customer.backend.model.Customer;
import com.customer.backend.repository.CustomerRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkUploadServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BulkUploadService bulkUploadService;

    // ── HELPER: build an in-memory .xlsx file ────────────────────────

    private MockMultipartFile buildExcelFile(List<String[]> rows) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Customers");

        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Date of Birth");
        header.createCell(2).setCellValue("NIC");

        // Data rows
        for (int i = 0; i < rows.size(); i++) {
            Row row = sheet.createRow(i + 1);
            String[] data = rows.get(i);
            for (int j = 0; j < data.length; j++) {
                if (data[j] != null) {
                    row.createCell(j).setCellValue(data[j]);
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "customers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );
    }

    // ── PROCESS EXCEL UPLOAD ─────────────────────────────────────────

    @Test
    @DisplayName("processExcelUpload: success with valid rows")
    void processExcelUpload_success() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Alice", "1990-05-20", "NIC001"});
        rows.add(new String[]{"Bob",   "1985-11-10", "NIC002"});

        MockMultipartFile file = buildExcelFile(rows);

        when(customerRepository.findExistingNics(anyList()))
                .thenReturn(new ArrayList<>());
        when(customerRepository.saveAll(anyList()))
                .thenReturn(new ArrayList<>());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(0, result.getSkippedCount());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("processExcelUpload: skips rows with missing name")
    void processExcelUpload_missingName_rowFails() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"", "1990-05-20", "NIC001"});

        MockMultipartFile file = buildExcelFile(rows);

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("Name is required"));
    }

    @Test
    @DisplayName("processExcelUpload: skips rows with missing DOB")
    void processExcelUpload_missingDob_rowFails() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Alice", "", "NIC001"});

        MockMultipartFile file = buildExcelFile(rows);

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getFailedCount());
        assertTrue(result.getErrors().get(0).contains("Date of Birth is required"));
    }

    @Test
    @DisplayName("processExcelUpload: skips rows with missing NIC")
    void processExcelUpload_missingNic_rowFails() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Alice", "1990-05-20", ""});

        MockMultipartFile file = buildExcelFile(rows);

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getFailedCount());
        assertTrue(result.getErrors().get(0).contains("NIC is required"));
    }

    @Test
    @DisplayName("processExcelUpload: fails rows with invalid date format")
    void processExcelUpload_invalidDateFormat_rowFails() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Alice", "20-05-1990", "NIC001"}); // wrong format

        MockMultipartFile file = buildExcelFile(rows);

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getFailedCount());
        assertTrue(result.getErrors().get(0).contains("Invalid date format"));
    }

    @Test
    @DisplayName("processExcelUpload: skips duplicate NIC within same file")
    void processExcelUpload_duplicateNicInFile_skipsSecond() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Alice", "1990-05-20", "DUPLICATE_NIC"});
        rows.add(new String[]{"Bob",   "1988-03-15", "DUPLICATE_NIC"});

        MockMultipartFile file = buildExcelFile(rows);

        when(customerRepository.findExistingNics(anyList()))
                .thenReturn(new ArrayList<>());
        when(customerRepository.saveAll(anyList()))
                .thenReturn(new ArrayList<>());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getSkippedCount());
        assertEquals(1, result.getSuccessCount());
    }

    @Test
    @DisplayName("processExcelUpload: skips rows where NIC already exists in DB")
    void processExcelUpload_nicExistsInDb_skipsRow() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Alice", "1990-05-20", "EXISTING_NIC"});

        MockMultipartFile file = buildExcelFile(rows);

        when(customerRepository.findExistingNics(anyList()))
                .thenReturn(Collections.singletonList("EXISTING_NIC"));

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getSkippedCount());
        verify(customerRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("processExcelUpload: handles mix of valid, failed, and skipped rows")
    void processExcelUpload_mixedRows() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Alice", "1990-05-20", "NIC001"});  // valid
        rows.add(new String[]{"",      "1990-05-20", "NIC002"});  // missing name — fail
        rows.add(new String[]{"Carol", "1992-08-15", "NIC003"});  // valid

        MockMultipartFile file = buildExcelFile(rows);

        when(customerRepository.findExistingNics(anyList()))
                .thenReturn(new ArrayList<>());
        when(customerRepository.saveAll(anyList()))
                .thenReturn(new ArrayList<>());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
    }

    // ── FLUSH CHUNK ──────────────────────────────────────────────────

    @Test
    @DisplayName("flushChunk: saves only customers whose NIC is not in DB")
    void flushChunk_savesNewCustomersOnly() {
        Customer c1 = new Customer();
        c1.setNic("NEW_NIC");

        Customer c2 = new Customer();
        c2.setNic("OLD_NIC");

        List<Customer> batch = Arrays.asList(c1, c2);
        Set<String> batchNics = new HashSet<>(Arrays.asList("NEW_NIC", "OLD_NIC"));

        BulkUploadResultDTO result = new BulkUploadResultDTO();

        when(customerRepository.findExistingNics(anyList()))
                .thenReturn(Collections.singletonList("OLD_NIC"));

        int saved = bulkUploadService.flushChunk(batch, batchNics, result);

        assertEquals(1, saved);
        assertEquals(1, result.getSkippedCount());
        verify(customerRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("flushChunk: saves nothing when all NICs already exist in DB")
    void flushChunk_allExisting_savesNothing() {
        Customer c1 = new Customer();
        c1.setNic("NIC_A");

        List<Customer> batch = Collections.singletonList(c1);
        Set<String> batchNics = new HashSet<>(Collections.singletonList("NIC_A"));

        BulkUploadResultDTO result = new BulkUploadResultDTO();

        when(customerRepository.findExistingNics(anyList()))
                .thenReturn(Collections.singletonList("NIC_A"));

        int saved = bulkUploadService.flushChunk(batch, batchNics, result);

        assertEquals(0, saved);
        verify(customerRepository, never()).saveAll(anyList());
    }
}