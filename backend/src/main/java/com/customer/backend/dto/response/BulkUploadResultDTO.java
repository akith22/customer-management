package com.customer.backend.dto.response;

import java.util.ArrayList;
import java.util.List;

public class BulkUploadResultDTO {

    private int totalRows;
    private int successCount;   // newly created
    private int updatedCount;   // existing records updated
    private int failedCount;    // parse / validation errors
    private List<String> errors = new ArrayList<>();

    public BulkUploadResultDTO() {}

    // ── Getters & Setters ────────────────────────────────────────────

    public int getTotalRows()                  { return totalRows; }
    public void setTotalRows(int totalRows)    { this.totalRows = totalRows; }

    public int getSuccessCount()               { return successCount; }
    public void setSuccessCount(int v)         { this.successCount = v; }

    public int getUpdatedCount()               { return updatedCount; }
    public void setUpdatedCount(int v)         { this.updatedCount = v; }

    public int getFailedCount()                { return failedCount; }
    public void setFailedCount(int v)          { this.failedCount = v; }

    public List<String> getErrors()            { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public void addError(String error)         { this.errors.add(error); }

    // ── Convenience incrementers (cleaner than get/set in loops) ────

    public void incrementSuccess() { this.successCount++; }
    public void incrementUpdated() { this.updatedCount++; }
    public void incrementFailed()  { this.failedCount++; }
}