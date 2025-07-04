package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.exception.ForbiddenActionException;
import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.mapper.AddressMapper;
import com.vodafone.ecommerce.model.dto.AddressRequestDTO;
import com.vodafone.ecommerce.model.entity.Address;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.repository.AddressRepository;
import com.vodafone.ecommerce.repository.CustomerProfileRepository;
import com.vodafone.ecommerce.service.AddressService;
import com.vodafone.ecommerce.service.UserService;
import com.vodafone.ecommerce.validation.AddressValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final CustomerProfileRepository customerProfileRepository;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final AddressValidator addressValidator;
    private final UserService userService;

    public void createAddress(CustomerProfile customerProfile, AddressRequestDTO request) {

        Address address = addressMapper.toEntity(request, customerProfile);

        CustomerProfile customer = customerProfileRepository.findByIdWithAddresses(customerProfile.getCustomerProfileId())
                .orElseThrow(() -> new NotFoundException("Customer profile not found"));

        List<Address> addresses = customer.getAddresses();

        if (addresses.isEmpty())
            address.setPrimary(true);

        if (request.isPrimary()) {
            for (Address add : addresses) {
                if (!add.isPrimary())
                    continue;
                add.setPrimary(false);
                addressRepository.save(add);
            }
        }
        addressRepository.save(address);
    }

    public void deleteAddressById(Long addressId) {
        Address address = addressValidator.validateAddressExistence(addressId);

        if (address.isPrimary()) {
            throw new ForbiddenActionException("Cannot delete primary address");
        }

        CustomerProfile customerProfile = userService.getLoggedInUser().getCustomerProfile();
        CustomerProfile customer = customerProfileRepository.findByIdWithAddresses(customerProfile.getCustomerProfileId())
                .orElseThrow(() -> new NotFoundException("Customer profile not found"));

        customer.getAddresses().remove(address);

        customerProfileRepository.save(customer);
    }
}
