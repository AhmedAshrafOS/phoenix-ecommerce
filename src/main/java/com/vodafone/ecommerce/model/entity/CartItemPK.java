package com.vodafone.ecommerce.model.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemPK implements Serializable {
    private Long cartId;
    private Long productId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CartItemPK that)) return false;
        return Objects.equals(cartId, that.cartId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId, productId);
    }
}
