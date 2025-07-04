package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDTO {

    @NotNull(message = "The Address is required")
    private AddressResponseDTO address;

    @NotNull(message = "The Payment Method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "The currency  is required")
    private String currency;

    @NotNull(message = "The email  is required")
    private String email;


}
