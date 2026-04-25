package com.customer.backend.dto.response;

import java.util.ArrayList;
import java.util.List;

public class BulkUploadResultDTO {

    private int totalRows;
    private int successCount;
    private int failedCount;
    private int skippedCount;       // rows with duplicate NIC skipped
    private List<String> errors = new ArrayList<>();

    public BulkUploadResultDTO() {}

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public void addError(String error) { this.errors.add(error); }
}