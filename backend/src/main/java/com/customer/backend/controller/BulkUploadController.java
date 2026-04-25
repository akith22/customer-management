package com.customer.backend.controller;

import com.customer.backend.dto.response.BulkUploadResultDTO;
import com.customer.backend.service.BulkUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers/bulk")
@CrossOrigin(origins = "*")
public class BulkUploadController {

    private final BulkUploadService bulkUploadService;

    public BulkUploadController(BulkUploadService bulkUploadService) {
        this.bulkUploadService = bulkUploadService;
    }

    // ── POST /api/customers/bulk/upload ──────────────────────────────
    // Accepts a multipart Excel file (.xlsx)
    // Excel column order: A=Name | B=DOB (yyyy-MM-dd) | C=NIC
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCustomers(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST,
                    "No file provided. Please attach an Excel (.xlsx) file.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null
                || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST,
                    "Invalid file type. Only .xlsx and .xls files are accepted.");
        }

        try {
            BulkUploadResultDTO result = bulkUploadService.processExcelUpload(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to process file: " + e.getMessage());
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