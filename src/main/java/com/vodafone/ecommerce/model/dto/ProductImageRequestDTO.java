package com.vodafone.ecommerce.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageRequestDTO {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotBlank(message = "Order Display is required")
    private Integer displayOrder;
}
