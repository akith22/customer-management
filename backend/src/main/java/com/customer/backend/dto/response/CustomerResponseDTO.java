package com.customer.backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Full detail DTO used in single customer view
public class CustomerResponseDTO {

    private Long id;
    private String name;
    private LocalDate dob;
    private String nic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> mobiles = new ArrayList<>();
    private List<AddressResponseDTO> addresses = new ArrayList<>();
    private List<FamilyMemberDTO> familyMembers = new ArrayList<>();

    public CustomerResponseDTO() {}

    // ── Nested DTO for family member preview ────────────────────────
    public static class FamilyMemberDTO {

        private Long id;
        private String name;
        private String nic;

        public FamilyMemberDTO() {}

        public FamilyMemberDTO(Long id, String name, String nic) {
            this.id = id;
            this.name = name;
            this.nic = nic;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getNic() { return nic; }
        public void setNic(String nic) { this.nic = nic; }
    }

    // ── Getters & Setters ────────────────────────────────────────────
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

    public List<String> getMobiles() { return mobiles; }
    public void setMobiles(List<String> mobiles) { this.mobiles = mobiles; }

    public List<AddressResponseDTO> getAddresses() { return addresses; }
    public void setAddresses(List<AddressResponseDTO> addresses) { this.addresses = addresses; }

    public List<FamilyMemberDTO> getFamilyMembers() { return familyMembers; }
    public void setFamilyMembers(List<FamilyMemberDTO> familyMembers) { this.familyMembers = familyMembers; }
}