package com.vodafone.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerProfileResponseDTO {
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    //    private List<String> addresses;
    private List<AddressResponseDTO> addresses;
}
