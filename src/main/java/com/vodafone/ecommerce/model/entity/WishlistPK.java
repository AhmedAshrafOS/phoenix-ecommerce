package com.vodafone.ecommerce.model.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class WishlistPK implements Serializable {
    private Long customerProfileId;
    private Long productId;
}
