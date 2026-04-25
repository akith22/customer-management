package com.customer.backend.controller;

import com.customer.backend.dto.request.CustomerRequestDTO;
import com.customer.backend.dto.response.CustomerResponseDTO;
import com.customer.backend.dto.response.CustomerSummaryDTO;
import com.customer.backend.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ── POST /api/customers ──────────────────────────────────────────
    // Create a new customer
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRequestDTO dto) {
        try {
            CustomerResponseDTO response = customerService.createCustomer(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    // ── PUT /api/customers/{id} ──────────────────────────────────────
    // Update an existing customer
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id,
                                            @RequestBody CustomerRequestDTO dto) {
        try {
            CustomerResponseDTO response = customerService.updateCustomer(id, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    // ── GET /api/customers/{id} ──────────────────────────────────────
    // View a single customer with full details
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        try {
            CustomerResponseDTO response = customerService.getCustomerById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    // ── GET /api/customers ───────────────────────────────────────────
    // Paginated table view
    // Example: GET /api/customers?page=0&size=20&sort=createdAt,desc
    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            Sort sort = direction.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CustomerSummaryDTO> response = customerService.getAllCustomers(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    // ── PRIVATE HELPER ───────────────────────────────────────────────
    private ResponseEntity<Map<String, String>> buildErrorResponse(HttpStatus status,
                                                                   String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
}