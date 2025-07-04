package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.WishlistResponseDTO;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.WishList;
import org.springframework.stereotype.Component;

@Component
public class WishlistMapper {

    public WishlistResponseDTO toDTO(WishList wishList) {
        Product product = wishList.getProductId();

        WishlistResponseDTO response = new WishlistResponseDTO();
        response.setProductId(product.getProductId());
        response.setProductName(product.getName());
        response.setPrice(product.getPrice());

        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            response.setImageUrl(product.getProductImages().get(0).getImageUrl());
        }

        return response;
    }
}
