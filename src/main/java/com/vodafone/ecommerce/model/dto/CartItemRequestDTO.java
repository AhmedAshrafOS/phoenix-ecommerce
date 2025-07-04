package com.vodafone.ecommerce.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // USED can't remove it
public class CartItemRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(1)
    @NotNull(message = "Quantity is required")
    private Integer quantity;
}
