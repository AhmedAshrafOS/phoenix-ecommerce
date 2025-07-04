package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.CartItemResponseDTO;
import com.vodafone.ecommerce.model.entity.CartItem;
import com.vodafone.ecommerce.model.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    private static Double getFinalUnitPrice(Product product) {
        if (product.getDiscountPercentage() == null || product.getDiscountPercentage() == 0) {
            return product.getPrice();
        }
        return product.getPrice() - (product.getPrice() * (product.getDiscountPercentage() / 100));
    }

    private static String getFirstImageUrl(Product product) {
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            return product.getProductImages().get(0).getImageUrl();
        }
        return null;
    }

    public CartItemResponseDTO toDTO(CartItem cartItem) {
        Product product = cartItem.getProductId();

        Double originalPrice = product.getPrice();
        Double finalUnitPrice = getFinalUnitPrice(product);
        Integer quantity = cartItem.getQuantity();

        return CartItemResponseDTO.builder()
                .productId(product.getProductId())
                .productName(product.getName())
                .originalPrice(originalPrice)
                .finalUnitPrice(finalUnitPrice)
                .quantity(quantity)
                .totalOriginalPrice(originalPrice * quantity)
                .totalFinalPrice(finalUnitPrice * quantity)
                .productImageUrl(getFirstImageUrl(product))
                .build();
    }
}
