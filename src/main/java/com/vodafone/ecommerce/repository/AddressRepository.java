package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.Address;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends BaseRepository<Address, Long> {

    Address findByAddressId(Long addressId);
}
