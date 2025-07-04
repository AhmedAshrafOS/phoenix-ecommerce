package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.WishList;
import com.vodafone.ecommerce.model.entity.WishlistPK;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishListRepository extends BaseRepository<WishList, WishlistPK> {

    List<WishList> findByCustomerProfileIdCustomerProfileId(Long customerProfileId);

    @Modifying
    @Query("DELETE FROM WishList w WHERE w.customerProfileId.customerProfileId = :customerId AND w.productId.productId = :productId")
    void deleteByCustomerProfileIdAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);

    boolean existsByCustomerProfileIdAndProductId(CustomerProfile customer, Product product);
}
