package com.customer.backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Lightweight DTO used in paginated table view
public class CustomerSummaryDTO {

    private Long id;
    private String name;
    private LocalDate dob;
    private String nic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerSummaryDTO() {}

    public CustomerSummaryDTO(Long id, String name, LocalDate dob,
                              String nic, LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.nic = nic;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}