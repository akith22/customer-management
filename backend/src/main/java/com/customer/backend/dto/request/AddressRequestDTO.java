package com.customer.backend.dto.request;


public class AddressRequestDTO {

    private String addressLine1;
    private String addressLine2;
    private Long cityId;

    public AddressRequestDTO() {}

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
}