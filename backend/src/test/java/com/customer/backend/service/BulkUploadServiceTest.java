package com.customer.backend.service;

import com.customer.backend.dto.response.BulkUploadResultDTO;
import com.customer.backend.model.Customer;
import com.customer.backend.repository.CustomerRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkUploadServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BulkUploadService bulkUploadService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bulkUploadService, "self", bulkUploadService);
    }

    @Test
    void processExcelUpload_allValidRows_allSucceed() throws Exception {
        List<String[]> rows = Arrays.asList(
                new String[]{"Alice", "1990-01-15", "199001512345"},
                new String[]{"Bob", "1985-06-20", "850620123V"},
                new String[]{"Carol", "2000-03-10", "200003101234"}
        );

        MockMultipartFile file = createExcelFile(rows);

        when(customerRepository.findAllByNicIn(anyList())).thenReturn(Collections.<Customer>emptyList());
        when(customerRepository.saveAll(anyList())).thenReturn(Collections.<Customer>emptyList());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void processExcelUpload_duplicateNicInDB_marksRowFailed() throws Exception {
        List<String[]> rows = Collections.singletonList(
                new String[]{"Alice", "1990-01-15", "199001512345"}
        );

        MockMultipartFile file = createExcelFile(rows);

        Customer existing = new Customer();
        existing.setNic("199001512345");

        when(customerRepository.findAllByNicIn(anyList())).thenReturn(Collections.singletonList(existing));

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getFailedCount());
        assertEquals(0, result.getSuccessCount());
        assertTrue(result.getErrors().get(0).contains("NIC already exists"));
    }

    @Test
    void processExcelUpload_missingName_rowFails() throws Exception {
        MockMultipartFile file = createExcelFile(Collections.singletonList(
                new String[]{"", "1990-01-15", "199001512345"}
        ));

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getFailedCount());
        assertTrue(result.getErrors().get(0).contains("Name is required"));
    }

    @Test
    void processExcelUpload_missingDob_rowFails() throws Exception {
        MockMultipartFile file = createExcelFile(Collections.singletonList(
                new String[]{"Alice", "", "199001512345"}
        ));

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getFailedCount());
        assertTrue(result.getErrors().get(0).contains("Date of Birth is required"));
    }

    @Test
    void processExcelUpload_invalidDateFormat_rowFails() throws Exception {
        MockMultipartFile file = createExcelFile(Collections.singletonList(
                new String[]{"Alice", "25-01-1990", "199001512345"}
        ));

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getFailedCount());
        assertTrue(result.getErrors().get(0).contains("Invalid date format"));
    }

    @Test
    void processExcelUpload_futureDob_rowFails() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        MockMultipartFile file = createExcelFile(Collections.singletonList(
                new String[]{"Alice", tomorrow, "199001512345"}
        ));

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getFailedCount());
        assertTrue(result.getErrors().get(0).contains("cannot be in the future"));
    }

    @Test
    void processExcelUpload_invalidNicFormat_rowFails() throws Exception {
        MockMultipartFile file = createExcelFile(Collections.singletonList(
                new String[]{"Alice", "1990-01-15", "12345"}
        ));

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getFailedCount());
        assertTrue(result.getErrors().get(0).contains("Invalid NIC format"));
    }

    @Test
    void processExcelUpload_nicWith9DigitsAndV_succeeds() throws Exception {
        MockMultipartFile file = createExcelFile(Collections.singletonList(
                new String[]{"Alice", "1990-01-15", "123456789V"}
        ));

        when(customerRepository.findAllByNicIn(anyList())).thenReturn(Collections.<Customer>emptyList());
        when(customerRepository.saveAll(anyList())).thenReturn(Collections.<Customer>emptyList());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
    }

    @Test
    void processExcelUpload_nicWith12Digits_succeeds() throws Exception {
        MockMultipartFile file = createExcelFile(Collections.singletonList(
                new String[]{"Alice", "1990-01-15", "200012345678"}
        ));

        when(customerRepository.findAllByNicIn(anyList())).thenReturn(Collections.<Customer>emptyList());
        when(customerRepository.saveAll(anyList())).thenReturn(Collections.<Customer>emptyList());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void processExcelUpload_duplicateNicWithinSameFile_keepLastRow() throws Exception {
        List<String[]> rows = Arrays.asList(
                new String[]{"Alice", "1990-01-15", "199001512345"},
                new String[]{"AliceUpdated", "1990-01-15", "199001512345"}
        );

        MockMultipartFile file = createExcelFile(rows);

        when(customerRepository.findAllByNicIn(anyList())).thenReturn(Collections.<Customer>emptyList());
        when(customerRepository.saveAll(anyList())).thenReturn(Collections.<Customer>emptyList());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        ArgumentCaptor<List<Customer>> captor =
            (ArgumentCaptor<List<Customer>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(customerRepository, times(1)).saveAll(captor.capture());

        List<Customer> savedCustomers = captor.getValue();
        assertEquals(1, savedCustomers.size());
        assertEquals("AliceUpdated", savedCustomers.get(0).getName());
        assertEquals(1, result.getSuccessCount());
    }

    @Test
    void processExcelUpload_mixedValidAndInvalid_correctCounts() throws Exception {
        List<String[]> rows = Arrays.asList(
                new String[]{"Alice", "1990-01-15", "199001512345"},
                new String[]{"", "1990-01-15", "200003101234"},
                new String[]{"Bob", "1985-06-20", "850620123V"}
        );

        MockMultipartFile file = createExcelFile(rows);

        when(customerRepository.findAllByNicIn(anyList())).thenReturn(Collections.<Customer>emptyList());
        when(customerRepository.saveAll(anyList())).thenReturn(Collections.<Customer>emptyList());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        assertEquals(3, result.getTotalRows());
    }

    @Test
    void processExcelUpload_emptyFile_returnsZeroRows() throws Exception {
        MockMultipartFile file = createExcelFile(Collections.<String[]>emptyList());

        BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);

        assertEquals(0, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void flushChunk_newCustomers_savesAll() {
        Customer c1 = new Customer();
        c1.setName("Alice");
        c1.setNic("199001512345");

        Customer c2 = new Customer();
        c2.setName("Bob");
        c2.setNic("850620123V");

        List<Customer> batch = Arrays.asList(c1, c2);
        Map<String, Integer> rowNumberByNic = new HashMap<String, Integer>();
        rowNumberByNic.put("199001512345", 2);
        rowNumberByNic.put("850620123V", 3);

        when(customerRepository.findAllByNicIn(anyList())).thenReturn(Collections.<Customer>emptyList());
        when(customerRepository.saveAll(anyList())).thenReturn(Collections.<Customer>emptyList());

        BulkUploadResultDTO result = new BulkUploadResultDTO();
        bulkUploadService.flushChunk(batch, rowNumberByNic, result);

        ArgumentCaptor<List<Customer>> captor =
            (ArgumentCaptor<List<Customer>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(customerRepository, times(1)).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals(2, result.getSuccessCount());
    }

    @Test
    void flushChunk_existingNic_incrementsFailed() {
        Customer c1 = new Customer();
        c1.setName("Alice");
        c1.setNic("199001512345");

        Customer existing = new Customer();
        existing.setNic("199001512345");

        List<Customer> batch = Collections.singletonList(c1);
        Map<String, Integer> rowNumberByNic = new HashMap<String, Integer>();
        rowNumberByNic.put("199001512345", 2);

        when(customerRepository.findAllByNicIn(anyList())).thenReturn(Collections.singletonList(existing));

        BulkUploadResultDTO result = new BulkUploadResultDTO();
        bulkUploadService.flushChunk(batch, rowNumberByNic, result);

        assertEquals(1, result.getFailedCount());
        assertEquals(0, result.getSuccessCount());
        assertFalse(result.getErrors().isEmpty());
        verify(customerRepository, never()).saveAll(anyList());
    }

    private MockMultipartFile createExcelFile(List<String[]> rows) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Sheet sheet = workbook.createSheet("Customers");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("DOB");
        header.createCell(2).setCellValue("NIC");

        for (int i = 0; i < rows.size(); i++) {
            Row row = sheet.createRow(i + 1);
            String[] data = rows.get(i);
            for (int j = 0; j < data.length; j++) {
                if (data[j] != null) {
                    row.createCell(j).setCellValue(data[j]);
                }
            }
        }

        workbook.write(baos);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                baos.toByteArray()
        );
    }
}
