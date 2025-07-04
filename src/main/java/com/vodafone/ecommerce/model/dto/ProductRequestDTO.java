package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.model.enums.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ProductRequestDTO {

    @NotBlank(message = "Low stock threshold is required")
    private String name;

    @NotBlank(message = "Features are required")
    @Size(max = 255, message = "Features must not exceed 255 characters")
    private String features;

    @NotBlank(message = "Specifications are required")
    @Size(max = 255, message = "Specifications must not exceed 255 characters")
    private String specs;

    @Min(1)
    @NotNull(message = "Price is required")
    private Double price;

    @Min(1)
    @NotNull(message = "Stock quantity is required and greater than 0")
    private Integer stockQuantity;

    @NotNull(message = "Low stock threshold is required")
    private Integer lowStockThreshold;

    @NotBlank(message = "Brand name  is required")
    private String brandName;

    @NotNull(message = "Category is required")
    private Category category;

    private Double discountPercentage = 0.0;

    private List<MultipartFile> imagesUrls;
}
