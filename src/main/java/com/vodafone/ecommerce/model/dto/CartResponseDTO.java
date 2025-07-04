package com.vodafone.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartResponseDTO {
    private Long cartId;
    private List<CartItemResponseDTO> cartItems;
}
