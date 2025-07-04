package com.vodafone.ecommerce;

import com.vodafone.ecommerce.exception.ForbiddenActionException;
import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.mapper.AddressMapper;
import com.vodafone.ecommerce.model.dto.AddressRequestDTO;
import com.vodafone.ecommerce.model.entity.Address;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.repository.AddressRepository;
import com.vodafone.ecommerce.repository.CustomerProfileRepository;
import com.vodafone.ecommerce.service.UserService;
import com.vodafone.ecommerce.service.impl.AddressServiceImpl;
import com.vodafone.ecommerce.validation.AddressValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock private CustomerProfileRepository customerProfileRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private AddressMapper addressMapper;
    @Mock private AddressValidator addressValidator;
    @Mock private UserService userService;

    @InjectMocks
    private AddressServiceImpl addressService;

    private CustomerProfile customerProfile;
    private AddressRequestDTO addressRequest;
    private Address newAddress;

    @BeforeEach
    void setUp() {
        customerProfile = new CustomerProfile();
        customerProfile.setCustomerProfileId(1L);
        customerProfile.setAddresses(new ArrayList<>());

        addressRequest = new AddressRequestDTO();
        addressRequest.setPrimary(false);

        newAddress = new Address();
        newAddress.setPrimary(false);
    }

    @Test
    void createAddress_ShouldSetPrimary_WhenFirstAddress() {
        when(addressMapper.toEntity(any(), any())).thenReturn(newAddress);
        when(customerProfileRepository.findByIdWithAddresses(1L)).thenReturn(Optional.of(customerProfile));

        addressService.createAddress(customerProfile, addressRequest);

        assertThat(newAddress.isPrimary()).isTrue();
        verify(addressRepository).save(newAddress);
    }

    @Test
    void createAddress_ShouldDemoteOldPrimary_WhenNewIsPrimary() {
        Address existing = new Address();
        existing.setPrimary(true);

        customerProfile.setAddresses(List.of(existing));
        addressRequest.setPrimary(true);
        newAddress.setPrimary(true);

        when(addressMapper.toEntity(any(), any())).thenReturn(newAddress);
        when(customerProfileRepository.findByIdWithAddresses(1L)).thenReturn(Optional.of(customerProfile));

        addressService.createAddress(customerProfile, addressRequest);

        assertThat(existing.isPrimary()).isFalse();
        verify(addressRepository).save(existing);
        verify(addressRepository).save(newAddress);
    }

    @Test
    void createAddress_ShouldThrow_WhenCustomerNotFound() {
        when(customerProfileRepository.findByIdWithAddresses(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                addressService.createAddress(customerProfile, addressRequest)
        ).isInstanceOf(NotFoundException.class)
                .hasMessage("Customer profile not found");
    }

    @Test
    void deleteAddressById_ShouldRemoveAddress_WhenValidAndNotPrimary() {
        Address address = new Address();
        address.setAddressId(5L);
        address.setPrimary(false);

        customerProfile.setAddresses(new ArrayList<>(List.of(address)));

        User user = new User();
        user.setCustomerProfile(customerProfile);

        when(addressValidator.validateAddressExistence(5L)).thenReturn(address);
        when(userService.getLoggedInUser()).thenReturn(user);
        when(customerProfileRepository.findByIdWithAddresses(1L)).thenReturn(Optional.of(customerProfile));

        addressService.deleteAddressById(5L);

        assertThat(customerProfile.getAddresses()).doesNotContain(address);
        verify(customerProfileRepository).save(customerProfile);
    }

    @Test
    void deleteAddressById_ShouldThrow_WhenAddressIsPrimary() {
        Address address = new Address();
        address.setPrimary(true);

        when(addressValidator.validateAddressExistence(anyLong())).thenReturn(address);

        assertThatThrownBy(() -> addressService.deleteAddressById(1L))
                .isInstanceOf(ForbiddenActionException.class)
                .hasMessage("Cannot delete primary address");
    }

    @Test
    void deleteAddressById_ShouldThrow_WhenCustomerNotFound() {
        Address address = new Address();
        address.setPrimary(false);

        when(addressValidator.validateAddressExistence(1L)).thenReturn(address);

        User user = new User();
        user.setCustomerProfile(customerProfile);

        when(userService.getLoggedInUser()).thenReturn(user);
        when(customerProfileRepository.findByIdWithAddresses(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddressById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Customer profile not found");
    }
}


