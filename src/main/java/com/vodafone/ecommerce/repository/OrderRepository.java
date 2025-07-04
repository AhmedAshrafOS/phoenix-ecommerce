package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends BaseRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.customerProfile.customerProfileId = :customerId")
    List<Order> findByCustomerId(Long customerId);
}
