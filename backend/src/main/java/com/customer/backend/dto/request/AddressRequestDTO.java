package com.customer.backend.dto.request;

public class AddressRequestDTO {

    private String addressLine1;
    private String addressLine2;
    private String cityName;      // user types city name directly
    private String countryName;   // user types country name directly

    public AddressRequestDTO() {}

    public String getAddressLine1()                    { return addressLine1; }
    public void setAddressLine1(String addressLine1)   { this.addressLine1 = addressLine1; }

    public String getAddressLine2()                    { return addressLine2; }
    public void setAddressLine2(String addressLine2)   { this.addressLine2 = addressLine2; }

    public String getCityName()                        { return cityName; }
    public void setCityName(String cityName)           { this.cityName = cityName; }

    public String getCountryName()                     { return countryName; }
    public void setCountryName(String countryName)     { this.countryName = countryName; }
}