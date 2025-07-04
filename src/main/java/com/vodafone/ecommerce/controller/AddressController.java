package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.AddressRequestDTO;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/address")
public class AddressController {

    private final AddressService addressService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void createAddress(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AddressRequestDTO request) {
        CustomerProfile customerProfile = userDetails.getUser().getCustomerProfile();
        addressService.createAddress(customerProfile, request);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping(path = "/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddressById(@PathVariable Long addressId) {
        addressService.deleteAddressById(addressId);
    }
}
