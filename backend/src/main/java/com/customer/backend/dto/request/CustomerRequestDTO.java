package com.customer.backend.dto.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerRequestDTO {

    // Mandatory fields
    private String name;
    private LocalDate dob;
    private String nic;

    // Optional fields
    private List<MobileRequestDTO> mobiles = new ArrayList<>();
    private List<AddressRequestDTO> addresses = new ArrayList<>();

    // List of existing customer IDs to link as family members
    private List<Long> familyMemberIds = new ArrayList<>();

    public CustomerRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public List<MobileRequestDTO> getMobiles() { return mobiles; }
    public void setMobiles(List<MobileRequestDTO> mobiles) { this.mobiles = mobiles; }

    public List<AddressRequestDTO> getAddresses() { return addresses; }
    public void setAddresses(List<AddressRequestDTO> addresses) { this.addresses = addresses; }

    public List<Long> getFamilyMemberIds() { return familyMemberIds; }
    public void setFamilyMemberIds(List<Long> familyMemberIds) { this.familyMemberIds = familyMemberIds; }
}