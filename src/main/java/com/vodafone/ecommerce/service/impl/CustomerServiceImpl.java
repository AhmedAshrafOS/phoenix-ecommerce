package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.mapper.AddressMapper;
import com.vodafone.ecommerce.mapper.CustomerMapper;
import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.Address;
import com.vodafone.ecommerce.model.entity.ConfirmationToken;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.model.enums.TokenType;
import com.vodafone.ecommerce.repository.AddressRepository;
import com.vodafone.ecommerce.repository.ConfirmationTokenRepository;
import com.vodafone.ecommerce.repository.CustomerProfileRepository;
import com.vodafone.ecommerce.repository.UserRepository;
import com.vodafone.ecommerce.service.CustomerService;
import com.vodafone.ecommerce.service.EmailService;
import com.vodafone.ecommerce.service.ShoppingCartService;
import com.vodafone.ecommerce.validation.UserValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerMapper customerMapper;
    private final UserRepository userRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final AddressMapper addressMapper;
    private final ShoppingCartService shoppingCartService;
    private final AddressRepository addressRepository;

    public CustomerProfile registerCustomer(CustomerRequestDTO request) {
        CustomerProfile customer = customerMapper.mapToCustomer(request);
        CustomerProfile savedCustomer = customerProfileRepository.save(customer);
        shoppingCartService.createCartForCustomerProfile(customer);
        return savedCustomer;
    }

    @Transactional
    public void updateCustomerProfile(CustomerUpdateRequestDTO request, User user) {
        log.info("Updating customer with ID: {}", user.getUserId());

        CustomerProfile customerProfile = customerProfileRepository.findByIdWithAddresses(user.getCustomerProfile().getCustomerProfileId())
                .orElseThrow(() -> new NotFoundException("Customer profile not found"));

        handleAddress(request, customerProfile);
        handleEmailUpdate(request.getEmail(), user);
        handlePhoneNumber(request.getPhone(), customerProfile);
        handleCustomerName(request.getFirstName(), request.getLastName(), customerProfile);
        handlePasswordUpdate(request, user);
        handleUsername(request.getUsername(), user);

        user.setCustomerProfile(customerProfile);

        customerProfileRepository.save(customerProfile);
        userRepository.save(user);

        log.info("Customer with ID {} updated successfully.", user.getUserId());
    }

    private void handleAddress(CustomerUpdateRequestDTO request, CustomerProfile customerProfile) {
        if (request.getAddress() == null) return;

        AddressResponseDTO requestAddress = request.getAddress();
        Address address = addressRepository.findByAddressId(requestAddress.getAddressId());

        if (address == null || !customerProfile.getAddresses().contains(address))
            throw new NotFoundException("Address not found in customer's address list");

        List<Address> addresses = customerProfile.getAddresses();

        if (request.getAddress().isPrimary()) {
            for (Address add : addresses) {
                if (!address.getAddressId().equals(add.getAddressId())) {
                    if (!add.isPrimary()) {
                        continue;
                    }
                    add.setPrimary(false);
                    addressRepository.save(add);
                }
            }
        }

        Address updatedAddress = addressMapper.fromResponseToEntity(requestAddress);
        if (address.isPrimary()) {
            updatedAddress.setPrimary(true);
        }
        updatedAddress.setCustomerProfile(customerProfile);
        addressRepository.save(updatedAddress);
    }

    public void handleUsername(String newUserName, User user) {
        if (newUserName != null && !newUserName.equals(user.getUsername())) {
            userValidator.validateUniqueUsername(newUserName);
            user.setUsername(newUserName);
        }
    }

    private void handlePhoneNumber(String newPhoneNumber, CustomerProfile customerProfile) {
        if (newPhoneNumber != null && !newPhoneNumber.equals(customerProfile.getPhoneNumber())) {
            customerProfile.setPhoneNumber(newPhoneNumber);
            log.debug("Updated phone number to {}", newPhoneNumber);
        }
    }

    private void handleEmailUpdate(String newEmail, User user) {
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            log.info("User {} requested email change from {} to {}", user.getUserId(), user.getEmail(), newEmail);

            userValidator.validateEmail(newEmail);
            ConfirmationToken confirmationToken = new ConfirmationToken(user, TokenType.EMAIL_VERIFICATION);
            confirmationToken.setNewEmail(newEmail);
            confirmationTokenRepository.save(confirmationToken);
            emailService.generateEmail(newEmail, confirmationToken);

            log.info("Email confirmation token sent to new email: {}", newEmail);
        }
    }

    public void handlePasswordUpdate(UserUpdateRequestDTO request, User user) {
        if (request.getNewPassword() != null && request.getOldPassword() != null && request.getConfirmPassword() != null) {
            userValidator.validatePasswordChange(request, user.getPassword());
            log.info("User {} is updating their password.", user.getUserId());
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            log.info("Password updated successfully for user {}", user.getUserId());
        }
    }

    private void handleCustomerName(String firstName, String lastName, CustomerProfile profile) {
        if (firstName != null && !firstName.equals(profile.getFirstName())) {
            profile.setFirstName(firstName);
            log.debug("Updated first name to {}", firstName);
        }
        if (lastName != null && !lastName.equals(profile.getLastName())) {
            profile.setLastName(lastName);
            log.debug("Updated last name to {}", lastName);
        }
    }

    public CustomerProfileResponseDTO getCustomerProfile(User user) {

        CustomerProfile profile = customerProfileRepository.findByIdWithAddresses(user.getCustomerProfile().getCustomerProfileId())
                .orElseThrow(() -> new NotFoundException("Customer profile not found"));

        CustomerProfileResponseDTO customerProfileResponseDTO = new CustomerProfileResponseDTO();
        customerProfileResponseDTO.setUserName(user.getUsername());
        customerProfileResponseDTO.setEmail(user.getEmail());
        customerProfileResponseDTO.setFirstName(profile.getFirstName());
        customerProfileResponseDTO.setLastName(profile.getLastName());
        customerProfileResponseDTO.setPhoneNumber(profile.getPhoneNumber());

        List<AddressResponseDTO> addressDTOs = profile.getAddresses().stream()
                .map(addressMapper::toDTO)
                .toList();

        customerProfileResponseDTO.setAddresses(addressDTOs);

        return customerProfileResponseDTO;
    }


    public void deleteCustomerProfile(CustomerProfile customer) {
        customerProfileRepository.delete(customer);
    }
}

