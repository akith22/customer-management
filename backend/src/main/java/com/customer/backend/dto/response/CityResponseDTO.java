package com.customer.backend.dto.response;


public class CityResponseDTO {

    private Long id;
    private String name;
    private String countryName;
    private String countryCode;

    public CityResponseDTO() {}

    public CityResponseDTO(Long id, String name, String countryName, String countryCode) {
        this.id = id;
        this.name = name;
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
