/*package com.customer.backend.controller;

import com.customer.backend.dto.request.CustomerRequestDTO;
import com.customer.backend.dto.response.BulkUploadResultDTO;
import com.customer.backend.dto.response.CustomerResponseDTO;
import com.customer.backend.dto.response.CustomerSummaryDTO;
import com.customer.backend.service.BulkUploadService;
import com.customer.backend.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock private CustomerService    customerService;
    @Mock private BulkUploadService  bulkUploadService;

    @InjectMocks private CustomerController    customerController;
    @InjectMocks private BulkUploadController  bulkUploadController;

    private MockMvc customerMvc;
    private MockMvc bulkMvc;

    private ObjectMapper objectMapper;
    private CustomerResponseDTO sampleResponse;
    private CustomerRequestDTO  sampleRequest;

    @BeforeEach
    void setUp() {
        customerMvc = MockMvcBuilders
                .standaloneSetup(customerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        bulkMvc = MockMvcBuilders
                .standaloneSetup(bulkUploadController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Sample response
        sampleResponse = new CustomerResponseDTO();
        sampleResponse.setId(1L);
        sampleResponse.setName("John Doe");
        sampleResponse.setNic("123456789V");
        sampleResponse.setDob(LocalDate.of(1990, 1, 15));
        sampleResponse.setCreatedAt(LocalDateTime.now());
        sampleResponse.setUpdatedAt(LocalDateTime.now());
        sampleResponse.setMobiles(new ArrayList<>());
        sampleResponse.setAddresses(new ArrayList<>());
        sampleResponse.setFamilyMembers(new ArrayList<>());

        // Sample request
        sampleRequest = new CustomerRequestDTO();
        sampleRequest.setName("John Doe");
        sampleRequest.setNic("123456789V");
        sampleRequest.setDob(LocalDate.of(1990, 1, 15));
        sampleRequest.setMobiles(new ArrayList<>());
        sampleRequest.setAddresses(new ArrayList<>());
        sampleRequest.setFamilyMemberIds(new ArrayList<>());
    }

    // ── POST /api/customers ──────────────────────────────────────────

    @Test
    @DisplayName("POST /api/customers: returns 201 on success")
    void createCustomer_returns201() throws Exception {
        when(customerService.createCustomer(any(CustomerRequestDTO.class)))
                .thenReturn(sampleResponse);

        customerMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.nic").value("123456789V"));

        verify(customerService, times(1)).createCustomer(any(CustomerRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/customers: returns 400 when NIC is duplicate")
    void createCustomer_duplicateNic_returns400() throws Exception {
        when(customerService.createCustomer(any(CustomerRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("NIC already exists."));

        customerMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NIC already exists."));
    }

    @Test
    @DisplayName("POST /api/customers: returns 500 on unexpected error")
    void createCustomer_unexpectedError_returns500() throws Exception {
        when(customerService.createCustomer(any(CustomerRequestDTO.class)))
                .thenThrow(new RuntimeException("Database down"));

        customerMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── PUT /api/customers/{id} ──────────────────────────────────────

    @Test
    @DisplayName("PUT /api/customers/{id}: returns 200 on success")
    void updateCustomer_returns200() throws Exception {
        when(customerService.updateCustomer(eq(1L), any(CustomerRequestDTO.class)))
                .thenReturn(sampleResponse);

        customerMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(customerService, times(1))
                .updateCustomer(eq(1L), any(CustomerRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/customers/{id}: returns 400 when customer not found")
    void updateCustomer_notFound_returns400() throws Exception {
        when(customerService.updateCustomer(eq(99L), any(CustomerRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Customer not found with id: 99"));

        customerMvc.perform(put("/api/customers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Customer not found with id: 99"));
    }

    // ── GET /api/customers/{id} ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/customers/{id}: returns 200 with full customer detail")
    void getCustomerById_returns200() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(sampleResponse);

        customerMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.nic").value("123456789V"));
    }

    @Test
    @DisplayName("GET /api/customers/{id}: returns 404 when not found")
    void getCustomerById_notFound_returns404() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new IllegalArgumentException("Customer not found with id: 99"));

        customerMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Customer not found with id: 99"));
    }

    // ── GET /api/customers ───────────────────────────────────────────

    @Test
    @DisplayName("GET /api/customers: returns 200 with paginated list")
    void getAllCustomers_returns200() throws Exception {
        CustomerSummaryDTO summary = new CustomerSummaryDTO(
                1L,
                "John Doe",
                LocalDate.of(1990, 1, 15),
                "123456789V",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Pageable pageable = PageRequest.of(0, 20);
        Page<CustomerSummaryDTO> page =
                new PageImpl<>(Collections.singletonList(summary), pageable, 1);

        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(page);

        customerMvc.perform(get("/api/customers")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/customers: returns empty content when no customers")
    void getAllCustomers_empty_returns200() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<CustomerSummaryDTO> emptyPage =
                new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(emptyPage);

        customerMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ── POST /api/customers/bulk/upload ──────────────────────────────

   /* @Test
    @DisplayName("POST /bulk/upload: returns 200 with result summary on success")
    void uploadCustomers_success_returns200() throws Exception {
        BulkUploadResultDTO result = new BulkUploadResultDTO();
        result.setTotalRows(3);
        result.setSuccessCount(3);
        result.setFailedCount(0);
        result.setSkippedCount(0);*/

       /* when(bulkUploadService.processExcelUpload(any()))
                .thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy content".getBytes()
        );

        bulkMvc.perform(multipart("/api/customers/bulk/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(3))
                .andExpect(jsonPath("$.successCount").value(3))
                .andExpect(jsonPath("$.failedCount").value(0));
    }

    @Test
    @DisplayName("POST /bulk/upload: returns 400 when no file provided")
    void uploadCustomers_noFile_returns400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "customers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        bulkMvc.perform(multipart("/api/customers/bulk/upload").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /bulk/upload: returns 400 when wrong file type")
    void uploadCustomers_wrongFileType_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers.csv",
                "text/csv",
                "Name,DOB,NIC".getBytes()
        );

        bulkMvc.perform(multipart("/api/customers/bulk/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /bulk/upload: returns 500 on service failure")
    void uploadCustomers_serviceFailure_returns500() throws Exception {
        when(bulkUploadService.processExcelUpload(any()))
                .thenThrow(new RuntimeException("Processing failed"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy".getBytes()
        );

        bulkMvc.perform(multipart("/api/customers/bulk/upload").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }
}*/