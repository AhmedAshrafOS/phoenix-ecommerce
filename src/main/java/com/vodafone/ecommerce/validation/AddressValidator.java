package com.vodafone.ecommerce.validation;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.model.entity.Address;
import com.vodafone.ecommerce.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddressValidator {
    private final AddressRepository addressRepository;

    public Address validateAddressExistence(Long id) {
        return addressRepository.findById(id).orElseThrow(() -> new NotFoundException("Address not found with ID: " + id));
    }
}
