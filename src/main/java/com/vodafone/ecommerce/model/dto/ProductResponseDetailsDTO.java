package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.model.enums.Category;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductResponseDetailsDTO {
    private Long productId;
    private String name;
    private String features;
    private String specs;
    private Double price;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Double averageRating;
    private Integer reviewCount;
    private String brandName;
    private Category category;
    private Double totalFinalPrice;
    private Double discountPercentage;
    private List<ProductImageResponseDTO> productImages;
}
