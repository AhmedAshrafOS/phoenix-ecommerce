package com.vodafone.ecommerce.validation;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerProfileValidator {

    private final CustomerProfileRepository customerProfileRepository;

    /**
     * Validates the existence of a customer profile by ID.
     *
     * @param customerProfileId the ID of the customer profile to check
     * @return the {@link CustomerProfile} if found
     * @throws NotFoundException if no customer profile exists with the given ID
     */
    public CustomerProfile requireExistingCustomer(Long customerProfileId) {
        return customerProfileRepository.findById(customerProfileId)
                .orElseThrow(() -> new NotFoundException("Customer profile not found for ID: " + customerProfileId));
    }
}
