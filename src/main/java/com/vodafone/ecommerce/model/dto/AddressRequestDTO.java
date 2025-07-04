package com.vodafone.ecommerce.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // Used in mapper can't remove!
public class AddressRequestDTO {

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Governorate is required")
    private String governorate;

    @NotBlank(message = "Building number is required")
    private String buildingNumber;

    @NotBlank(message = "Apartment number is required")
    private String apartmentNumber;

    @NotBlank(message = "Floor is required")
    private String floor;

    @NotBlank(message = "Country is required")
    private String country;

    private boolean isPrimary;
}
