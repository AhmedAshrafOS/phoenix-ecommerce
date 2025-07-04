package com.vodafone.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CartItemPK.class)
@Table(name = "cart_items")
@EntityListeners(AuditingEntityListener.class)
public class CartItem {
    @Id
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCart cartId;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product productId;

    private Integer quantity;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    @LastModifiedDate
    private LocalDate updatedDate;
}
