package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.ProductImageResponseDTO;
import com.vodafone.ecommerce.model.dto.ProductRequestDTO;
import com.vodafone.ecommerce.model.dto.ProductResponseDTO;
import com.vodafone.ecommerce.model.dto.ProductResponseDetailsDTO;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductMapper {

    public Product mapToProduct(ProductRequestDTO request) {
        List<ProductImage> productImages = new ArrayList<>();
        return Product.builder()
                .name(request.getName())
                .features(request.getFeatures())
                .specs(request.getSpecs())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .lowStockThreshold(request.getLowStockThreshold())
                .brandName(request.getBrandName())
                .category(request.getCategory())
                .discountPercentage(request.getDiscountPercentage())
                .productImages(productImages)
                .build();
    }

    public ProductResponseDetailsDTO mapToProductResponseDetailsDTO(Product product) {

        List<ProductImageResponseDTO> imagesResponse = new ArrayList<>();
        for (ProductImage productImage : product.getProductImages()) {
            ProductImageResponseDTO imageResponse = new ProductImageResponseDTO();
            imageResponse.setImageUrl(productImage.getImageUrl());
            imageResponse.setImageId(productImage.getImageId());
            imageResponse.setDisplayOrder(productImage.getDisplayOrder());
            imagesResponse.add(imageResponse);
        }

        ProductResponseDetailsDTO response = new ProductResponseDetailsDTO();
        response.setProductId(product.getProductId());
        response.setName(product.getName());
        response.setFeatures(product.getFeatures());
        response.setSpecs(product.getSpecs());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setLowStockThreshold(product.getLowStockThreshold());
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());
        response.setBrandName(product.getBrandName());
        response.setCategory(product.getCategory());
        response.setDiscountPercentage(product.getDiscountPercentage());
        response.setProductImages(imagesResponse);
        return response;
    }

    public ProductResponseDTO mapToProductResponseDTO(Product product) {
        ProductImageResponseDTO imageResponse = new ProductImageResponseDTO();
        imageResponse.setImageUrl(product.getProductImages().get(0).getImageUrl());
        imageResponse.setImageId(product.getProductImages().get(0).getImageId());
        imageResponse.setDisplayOrder(product.getProductImages().get(0).getDisplayOrder());

        ProductResponseDTO response = new ProductResponseDTO();
        response.setProductId(product.getProductId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setLowStockThreshold(product.getLowStockThreshold());
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());
        response.setBrandName(product.getBrandName());
        response.setCategory(product.getCategory());
        response.setDiscountPercentage(product.getDiscountPercentage());
        response.setProductImage(imageResponse);
        return response;
    }

    public ProductResponseDTO mapToProductSearchResponseDTO(Product product) {

        ProductImageResponseDTO imageResponse = new ProductImageResponseDTO();
        imageResponse.setImageUrl(product.getProductImages().get(0).getImageUrl());
        imageResponse.setImageId(product.getProductImages().get(0).getImageId());
        imageResponse.setDisplayOrder(product.getProductImages().get(0).getDisplayOrder());

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setLowStockThreshold(product.getLowStockThreshold());
        dto.setAverageRating(product.getAverageRating());
        dto.setReviewCount(product.getReviewCount());
        dto.setBrandName(product.getBrandName());
        dto.setCategory(product.getCategory());
        dto.setDiscountPercentage(product.getDiscountPercentage());

        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            dto.setProductImage(imageResponse);
        }

        return dto;
    }
}
