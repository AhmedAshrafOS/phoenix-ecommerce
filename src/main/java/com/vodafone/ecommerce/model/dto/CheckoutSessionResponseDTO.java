package com.vodafone.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutSessionResponseDTO {
    private String checkoutUrl;

    public CheckoutSessionResponseDTO(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }
}
