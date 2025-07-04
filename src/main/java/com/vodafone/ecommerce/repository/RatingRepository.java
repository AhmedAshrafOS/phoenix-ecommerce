package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.Order;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.Rating;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends BaseRepository<Rating, Long> {

    Optional<Rating> findByCustomerProfileAndProductAndOrder(CustomerProfile customerProfile, Product product, Order order);

    List<Rating> findByProduct_ProductId(Long productId);
}
