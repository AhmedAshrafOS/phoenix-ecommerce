package com.vodafone.ecommerce.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDTO {
    @NotBlank(message = "Comment is required")
    private String comment;

    @NotNull(message = "Order ID is required")
    private Long orderId;
}
