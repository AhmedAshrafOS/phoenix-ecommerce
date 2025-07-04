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
@Table(name = "wish_lists")
@IdClass(WishlistPK.class)
@EntityListeners(AuditingEntityListener.class)
public class WishList {

    @Id
    @ManyToOne
    @JoinColumn(name = "customer_profile_id")
    private CustomerProfile customerProfileId;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product productId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    @LastModifiedDate
    private LocalDate updatedDate;
}
