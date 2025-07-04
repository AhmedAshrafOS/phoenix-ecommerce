package com.vodafone.ecommerce;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.mapper.AddressMapper;
import com.vodafone.ecommerce.mapper.CustomerMapper;
import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.*;
import com.vodafone.ecommerce.repository.*;
import com.vodafone.ecommerce.service.*;
import com.vodafone.ecommerce.service.impl.CustomerServiceImpl;
import com.vodafone.ecommerce.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock private CustomerProfileRepository customerProfileRepository;
    @Mock private CustomerMapper customerMapper;
    @Mock private UserRepository userRepository;
    @Mock private ConfirmationTokenRepository confirmationTokenRepository;
    @Mock private EmailService emailService;
    @Mock private UserValidator userValidator;
    @Mock private AddressMapper addressMapper;
    @Mock private ShoppingCartService shoppingCartService;
    @Mock private AddressRepository addressRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    private CustomerProfile customer;
    private User user;

    @BeforeEach
    void setUp() {
        customer = new CustomerProfile();
        customer.setCustomerProfileId(1L);
        customer.setAddresses(new ArrayList<>());

        user = new User();
        user.setUserId(100L);
        user.setEmail("old@email.com");
        user.setUsername("olduser");
        user.setPassword("encoded-pass");
        user.setCustomerProfile(customer);
    }

    @Test
    void registerCustomer_shouldSaveAndCreateCart() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        when(customerMapper.mapToCustomer(any())).thenReturn(customer);
        when(customerProfileRepository.save(customer)).thenReturn(customer);

        CustomerProfile result = customerService.registerCustomer(request);

        assertThat(result).isEqualTo(customer);
        verify(shoppingCartService).createCartForCustomerProfile(customer);
    }

    @Test
    void getCustomerProfile_shouldReturnMappedDTO() {
        Address address = new Address();
        address.setStreet("Test Street");
        customer.setAddresses(List.of(address));

        when(customerProfileRepository.findByIdWithAddresses(1L)).thenReturn(Optional.of(customer));
        when(addressMapper.toDTO(any())).thenReturn(new AddressResponseDTO());

        CustomerProfileResponseDTO result = customerService.getCustomerProfile(user);

        assertThat(result.getUserName()).isEqualTo("olduser");
        assertThat(result.getEmail()).isEqualTo("old@email.com");
    }

    @Test
    void getCustomerProfile_shouldThrow_whenNotFound() {
        when(customerProfileRepository.findByIdWithAddresses(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerProfile(user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Customer profile not found");
    }

    @Nested
    class UpdateCustomerTests {

        private CustomerUpdateRequestDTO updateRequest;

        @BeforeEach
        void init() {
            updateRequest = new CustomerUpdateRequestDTO();
            when(customerProfileRepository.findByIdWithAddresses(1L)).thenReturn(Optional.of(customer));
        }

        @Test
        void shouldUpdateUsernameIfDifferent() {
            updateRequest.setUsername("newuser");
            customerService.updateCustomerProfile(updateRequest, user);
            verify(userValidator).validateUniqueUsername("newuser");
            assertThat(user.getUsername()).isEqualTo("newuser");
        }

        @Test
        void shouldUpdatePhoneNumber() {
            updateRequest.setPhone("123456789");
            customer.setPhoneNumber("000000000");
            customerService.updateCustomerProfile(updateRequest, user);
            assertThat(customer.getPhoneNumber()).isEqualTo("123456789");
        }

        @Test
        void shouldSendEmailConfirmationTokenWhenEmailChanged() {
            updateRequest.setEmail("new@email.com");
            customerService.updateCustomerProfile(updateRequest, user);
            verify(emailService).generateEmail(eq("new@email.com"), any());
        }

        @Test
        void shouldUpdatePasswordWhenValid() {
            updateRequest.setOldPassword("old");
            updateRequest.setNewPassword("new");
            updateRequest.setConfirmPassword("new");

            when(passwordEncoder.encode("new")).thenReturn("encoded-new");

            customerService.updateCustomerProfile(updateRequest, user);

            verify(userValidator).validatePasswordChange(eq(updateRequest), eq("encoded-pass"));
            verify(passwordEncoder).encode("new");
            verify(userRepository).save(user);
            assertThat(user.getPassword()).isEqualTo("encoded-new");
        }

        @Test
        void shouldUpdateNameIfChanged() {
            updateRequest.setFirstName("John");
            updateRequest.setLastName("Doe");
            customerService.updateCustomerProfile(updateRequest, user);
            assertThat(customer.getFirstName()).isEqualTo("John");
            assertThat(customer.getLastName()).isEqualTo("Doe");
        }

        @Test
        void shouldUpdatePrimaryAddressCorrectly() {
            AddressResponseDTO addressDTO = new AddressResponseDTO();
            addressDTO.setAddressId(1L);
            addressDTO.setPrimary(true);

            Address addressToUpdate = new Address();
            addressToUpdate.setAddressId(1L);
            addressToUpdate.setPrimary(false);

            Address oldPrimary = new Address();
            oldPrimary.setAddressId(2L);
            oldPrimary.setPrimary(true);

            customer.setAddresses(new ArrayList<>(List.of(oldPrimary, addressToUpdate)));

            updateRequest.setAddress(addressDTO);

            when(addressRepository.findByAddressId(1L)).thenReturn(addressToUpdate);
            when(addressMapper.fromResponseToEntity(addressDTO)).thenReturn(addressToUpdate);

            customerService.updateCustomerProfile(updateRequest, user);

            assertThat(oldPrimary.isPrimary()).isFalse();
            verify(addressRepository).save(oldPrimary);
            verify(addressRepository).save(addressToUpdate);
        }

        @Test
        void shouldThrowIfAddressNotFound() {
            AddressResponseDTO addressDTO = new AddressResponseDTO();
            addressDTO.setAddressId(99L);
            updateRequest.setAddress(addressDTO);
            when(addressRepository.findByAddressId(99L)).thenReturn(null);

            assertThatThrownBy(() -> customerService.updateCustomerProfile(updateRequest, user))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Address not found in customer's address list");
        }
    }

    @Test
    void deleteCustomerProfile_shouldDeleteCustomer() {
        customerService.deleteCustomerProfile(customer);
        verify(customerProfileRepository).delete(customer);
    }

    @Test
    void updateCustomerProfile_shouldThrow_whenCustomerNotFound() {
        CustomerUpdateRequestDTO updateRequest = new CustomerUpdateRequestDTO();
        when(customerProfileRepository.findByIdWithAddresses(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomerProfile(updateRequest, user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Customer profile not found");
    }
}



