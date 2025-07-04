package com.vodafone.ecommerce.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "addresses")
@EntityListeners(AuditingEntityListener.class)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @Column(nullable = false)
    private String street;

    @NotNull
    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String governorate;

    @Column(nullable = false, length = 100)
    private String buildingNumber;

    @Column(nullable = false, length = 50)
    private String apartmentNumber;

    @Column(nullable = false, length = 50)
    private String floor;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false)
    private boolean isPrimary;

    @ManyToOne
    @JoinColumn(name = "customer_profile_id", nullable = false)
    private CustomerProfile customerProfile;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    @LastModifiedDate
    private LocalDate updatedDate;
}
