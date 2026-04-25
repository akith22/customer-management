package com.customer.backend.dto.response;

public class AddressResponseDTO {

    private Long id;
    private String addressLine1;
    private String addressLine2;
    private CityResponseDTO city;

    public AddressResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public CityResponseDTO getCity() { return city; }
    public void setCity(CityResponseDTO city) { this.city = city; }
}