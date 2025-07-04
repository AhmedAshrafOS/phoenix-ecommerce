package com.vodafone.ecommerce.model.entity;

import com.vodafone.ecommerce.model.enums.Category;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    private Category category;

    private Double discountPercentage;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    @LastModifiedDate
    private LocalDate updatedDate;

    /**
     * Calculates the final price of the product after applying the discount.
     * If no discount is applied (discountPercentage is null or zero), it returns the original price.
     *
     * @return final price after discount, or the original price if no discount is available
     */
    public Double getFinalUnitPrice() {
        if (discountPercentage == null || discountPercentage == 0) {
            return price;
        }
        return price - (price * (discountPercentage / 100));
    }
}
