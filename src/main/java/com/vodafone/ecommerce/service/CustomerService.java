package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.CustomerProfileResponseDTO;
import com.vodafone.ecommerce.model.dto.CustomerRequestDTO;
import com.vodafone.ecommerce.model.dto.CustomerUpdateRequestDTO;
import com.vodafone.ecommerce.model.dto.UserUpdateRequestDTO;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.User;

public interface CustomerService {

    CustomerProfile registerCustomer(CustomerRequestDTO request);

    void updateCustomerProfile(CustomerUpdateRequestDTO request, User user);

    void handleUsername(String newUserName, User user);

    void handlePasswordUpdate(UserUpdateRequestDTO request, User user);

    CustomerProfileResponseDTO getCustomerProfile(User user);

    void deleteCustomerProfile(CustomerProfile customer);
}
