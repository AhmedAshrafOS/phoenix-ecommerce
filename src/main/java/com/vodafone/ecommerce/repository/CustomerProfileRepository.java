package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.CustomerProfile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends BaseRepository<CustomerProfile, Long> {

    Boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT cp FROM CustomerProfile cp LEFT JOIN FETCH cp.addresses WHERE cp.customerProfileId = :id")
    Optional<CustomerProfile> findByIdWithAddresses(Long id);
}
