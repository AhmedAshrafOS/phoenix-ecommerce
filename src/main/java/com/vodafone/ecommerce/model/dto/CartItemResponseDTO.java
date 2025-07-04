package com.vodafone.ecommerce.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    private Long productId;

    private String productName;

    private Double originalPrice;

    private Double finalUnitPrice;

    private Integer quantity;

    private Double totalOriginalPrice;

    private Double totalFinalPrice;

    private String productImageUrl;
}

