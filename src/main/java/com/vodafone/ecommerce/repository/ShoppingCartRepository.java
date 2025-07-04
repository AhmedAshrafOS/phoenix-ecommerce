package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.ShoppingCart;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends BaseRepository<ShoppingCart, Long> {

    Optional<ShoppingCart> findByCustomerProfile_CustomerProfileId(Long customerProfileId);

    ShoppingCart findByCustomerProfile(CustomerProfile customerProfile);
}
