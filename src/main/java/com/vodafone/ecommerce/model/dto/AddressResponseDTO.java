package com.vodafone.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressResponseDTO {
    private Long addressId;
    private String street;
    private String city;
    private String governorate;
    private String buildingNumber;
    private String apartmentNumber;
    private String floor;
    private String country;
    private boolean isPrimary;

    @Override
    public String toString() {
        return String.format(
                "Id = '%s' ,street='%s', city='%s', governorate='%s', buildingNumber='%s', apartmentNumber='%s', floor='%s', country='%s', isPrimary='%s']",
                addressId, street, city, governorate, buildingNumber, apartmentNumber, floor, country, isPrimary
        );
    }
}
