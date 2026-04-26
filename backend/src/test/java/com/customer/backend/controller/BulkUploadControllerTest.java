package com.customer.backend.controller;

import com.customer.backend.dto.response.BulkUploadResultDTO;
import com.customer.backend.service.BulkUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BulkUploadController.class)
class BulkUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BulkUploadService bulkUploadService;

    @Test
    void uploadCustomers_validXlsxFile_returns200() throws Exception {
        BulkUploadResultDTO result = new BulkUploadResultDTO();
        result.setTotalRows(3);
        result.setSuccessCount(3);
        result.setFailedCount(0);

        when(bulkUploadService.processExcelUpload(any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/customers/bulk/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(3));
    }

    @Test
    void uploadCustomers_emptyFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/customers/bulk/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("No file provided")));
    }

    @Test
    void uploadCustomers_wrongFileType_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                "name,dob,nic".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/customers/bulk/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Invalid file type")));
    }

    @Test
    void uploadCustomers_serviceThrowsException_returns500() throws Exception {
        when(bulkUploadService.processExcelUpload(any())).thenThrow(new RuntimeException("Parse error"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/customers/bulk/upload").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Failed to process file")));
    }

    @Test
    void uploadCustomers_xlsFileAccepted_returns200() throws Exception {
        BulkUploadResultDTO result = new BulkUploadResultDTO();
        result.setTotalRows(1);
        result.setSuccessCount(1);
        result.setFailedCount(0);

        when(bulkUploadService.processExcelUpload(any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xls",
                "application/vnd.ms-excel",
                "dummy".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/customers/bulk/upload").file(file))
                .andExpect(status().isOk());
    }
}
