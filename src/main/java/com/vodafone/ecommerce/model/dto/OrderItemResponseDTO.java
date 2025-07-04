package com.vodafone.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponseDTO {

    private Long productId;

    private String name;

    private Integer quantity;

    private Double unitPrice;

    private String imageUrl;

    private Double discountAmount;
}