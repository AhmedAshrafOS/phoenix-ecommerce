package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.AddressRequestDTO;
import com.vodafone.ecommerce.model.entity.CustomerProfile;

public interface AddressService {

    void createAddress(CustomerProfile customerProfile, AddressRequestDTO request);

    void deleteAddressById(Long addressId);
}
