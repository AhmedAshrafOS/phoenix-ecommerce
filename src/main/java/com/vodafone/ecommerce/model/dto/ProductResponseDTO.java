package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.model.enums.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDTO {
    private Long productId;
    private String name;
    private Double price;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Double averageRating;
    private Integer reviewCount;
    private String brandName;
    private Category category;
    private Double discountPercentage;
    private ProductImageResponseDTO productImage;
}
