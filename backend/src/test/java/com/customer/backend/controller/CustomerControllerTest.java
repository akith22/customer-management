package com.customer.backend.controller;

import com.customer.backend.dto.request.CustomerRequestDTO;
import com.customer.backend.dto.response.AddressResponseDTO;
import com.customer.backend.dto.response.CityResponseDTO;
import com.customer.backend.dto.response.CustomerResponseDTO;
import com.customer.backend.dto.response.CustomerSummaryDTO;
import com.customer.backend.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void createCustomer_validRequest_returns201() throws Exception {
        when(customerService.createCustomer(any(CustomerRequestDTO.class))).thenReturn(buildCustomerResponse());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(buildCustomerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void createCustomer_serviceThrowsIllegalArgument_returns400() throws Exception {
        when(customerService.createCustomer(any(CustomerRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("NIC already exists"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(buildCustomerRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("NIC already exists")));
    }

    @Test
    void createCustomer_serviceThrowsRuntimeException_returns500() throws Exception {
        when(customerService.createCustomer(any(CustomerRequestDTO.class)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(buildCustomerRequest())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateCustomer_validRequest_returns200() throws Exception {
        when(customerService.updateCustomer(eq(1L), any(CustomerRequestDTO.class))).thenReturn(buildCustomerResponse());

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(buildCustomerRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nic").value("199001512345"));
    }

    @Test
    void updateCustomer_notFound_returns400() throws Exception {
        when(customerService.updateCustomer(eq(1L), any(CustomerRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("not found"));

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(buildCustomerRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCustomerById_exists_returns200() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(buildCustomerResponse());

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getCustomerById_notFound_returns404() throws Exception {
        when(customerService.getCustomerById(1L)).thenThrow(new IllegalArgumentException("not found"));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCustomers_defaultParams_returns200() throws Exception {
        Page<CustomerSummaryDTO> page = new PageImpl<>(Collections.singletonList(buildSummary()));
        when(customerService.getAllCustomers(any(Pageable.class), isNull())).thenReturn(page);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice"));
    }

    @Test
    void getAllCustomers_withNameFilter_passesNameToService() throws Exception {
        Page<CustomerSummaryDTO> page = new PageImpl<>(Collections.singletonList(buildSummary()));
        when(customerService.getAllCustomers(any(Pageable.class), eq("Alice"))).thenReturn(page);

        mockMvc.perform(get("/api/customers").param("name", "Alice"))
                .andExpect(status().isOk());

        verify(customerService).getAllCustomers(any(Pageable.class), eq("Alice"));
    }

    @Test
    void getAllCustomers_withSortAsc_builds200() throws Exception {
        Page<CustomerSummaryDTO> page = new PageImpl<>(Collections.singletonList(buildSummary()));
        when(customerService.getAllCustomers(any(Pageable.class), isNull())).thenReturn(page);

        mockMvc.perform(get("/api/customers")
                        .param("direction", "asc")
                        .param("sortBy", "name"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllCustomers_withPagination_returns200() throws Exception {
        Page<CustomerSummaryDTO> page = new PageImpl<>(Collections.singletonList(buildSummary()));
        when(customerService.getAllCustomers(any(Pageable.class), isNull())).thenReturn(page);

        mockMvc.perform(get("/api/customers")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    private CustomerRequestDTO buildCustomerRequest() {
        CustomerRequestDTO dto = new CustomerRequestDTO();
        dto.setName("Alice");
        dto.setDob(LocalDate.of(1990, 1, 15));
        dto.setNic("199001512345");
        dto.setMobiles(new ArrayList<>());
        dto.setAddresses(new ArrayList<>());
        dto.setFamilyMemberIds(new ArrayList<>());
        return dto;
    }

    private CustomerResponseDTO buildCustomerResponse() {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(1L);
        dto.setName("Alice");
        dto.setNic("199001512345");
        dto.setDob(LocalDate.of(1990, 1, 15));
        dto.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setMobiles(Collections.singletonList("0771234567"));

        AddressResponseDTO address = new AddressResponseDTO();
        address.setId(1L);
        address.setAddressLine1("123 Main Street");
        address.setAddressLine2("Apartment 4B");
        address.setCity(new CityResponseDTO(1L, "Colombo", "Sri Lanka", "LK"));
        dto.setAddresses(Collections.singletonList(address));

        dto.setFamilyMembers(Collections.emptyList());
        return dto;
    }

    private CustomerSummaryDTO buildSummary() {
        return new CustomerSummaryDTO(
                1L,
                "Alice",
                LocalDate.of(1990, 1, 15),
                "199001512345",
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 1, 10, 0)
        );
    }
}
