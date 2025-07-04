package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.ProductImageRequestDTO;
import com.vodafone.ecommerce.model.dto.ProductImageResponseDTO;
import com.vodafone.ecommerce.model.entity.ProductImage;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {
    public ProductImage mapToProductImage(ProductImageRequestDTO request) {
        return ProductImage.builder().imageUrl(request.getImageUrl()).displayOrder(request.getDisplayOrder()).build();
    }

    public ProductImageResponseDTO mapToProductImageResponseDTO(ProductImage productImage) {
        ProductImageResponseDTO response = new ProductImageResponseDTO();
        response.setImageId(productImage.getImageId());
        response.setImageUrl(productImage.getImageUrl());
        response.setDisplayOrder(productImage.getDisplayOrder());
        return response;
    }
}
