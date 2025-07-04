package com.vodafone.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageResponseDTO {
    private Long imageId;
    private String imageUrl;
    private Integer displayOrder;
}
