package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.CartResponseDTO;
import com.vodafone.ecommerce.model.entity.CustomerProfile;

public interface ShoppingCartService {

    void createCartForCustomerProfile(CustomerProfile customerProfile);

    Long getCartIdByCustomerId(Long customerId);

    CartResponseDTO getCartByCustomerId(Long customerId);
}
