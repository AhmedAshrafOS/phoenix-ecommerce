package com.vodafone.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistResponseDTO {

    private Long productId;

    private String productName;

    private Double price;

    private String imageUrl;
}
