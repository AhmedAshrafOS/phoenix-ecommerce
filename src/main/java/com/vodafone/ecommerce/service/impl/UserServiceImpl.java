package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.mapper.UserMapper;
import com.vodafone.ecommerce.model.dto.AdminRequestDTO;
import com.vodafone.ecommerce.model.dto.AdminResponseDTO;
import com.vodafone.ecommerce.model.dto.AdminUpdateRequestDTO;
import com.vodafone.ecommerce.model.dto.CustomerRequestDTO;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.ShoppingCart;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.model.enums.Role;
import com.vodafone.ecommerce.repository.ShoppingCartRepository;
import com.vodafone.ecommerce.repository.UserRepository;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.security.repository.RefreshTokenRepository;
import com.vodafone.ecommerce.service.CustomerService;
import com.vodafone.ecommerce.service.UserService;
import com.vodafone.ecommerce.validation.UserValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.vodafone.ecommerce.model.enums.AccountStatus.ACTIVE;
import static com.vodafone.ecommerce.model.enums.AccountStatus.DEACTIVATED;
import static com.vodafone.ecommerce.model.enums.Role.ADMIN;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserMapper userMapper;
    private final CustomerService customerService;
    private final UserValidator userValidator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    public User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return userDetails.getUser();
    }

    public User registerUser(CustomerRequestDTO request) {

        userValidator.validateEmail(request.getEmail());
        userValidator.validateUniqueUsername(request.username);
        userValidator.validateUniquePhone(request.getPhoneNumber());

        User user = userMapper.mapToUserForCreateUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        CustomerProfile customer = customerService.registerCustomer(request);
        user.setCustomerProfile(customer);
        user.setAccountStatus(DEACTIVATED);

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(User user) {
        refreshTokenRepository.deleteByUser(user);

        CustomerProfile customer = user.getCustomerProfile();
        if (customer != null) {
            user.setCustomerProfile(null);

            ShoppingCart cart = shoppingCartRepository.findByCustomerProfile(customer);
            shoppingCartRepository.delete(cart);

            customerService.deleteCustomerProfile(customer);
        }
        userRepository.delete(user);
    }

    public void addAdmin(AdminRequestDTO request) {
        userValidator.validateEmail(request.getEmail());
        userValidator.validateUniqueUsername(request.getUsername());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userMapper.mapToUserAdmin(request);
        user.setPassword(encodedPassword);

        user.setAccountStatus(ACTIVE);
        user.setRole(ADMIN);
        userRepository.save(user);

    }

    public List<AdminResponseDTO> getAllAdmins() {
        List<User> admins = userRepository.findAllByRoleIn(List.of(Role.ADMIN, Role.SUPER_ADMIN));
        return userMapper.toAdminResponseDTOList(admins);
    }

    public AdminResponseDTO getAdminProfile() {
        User user = getLoggedInUser();
        return userMapper.toAdminResponseDTO(user);
    }

    public void updateAdmin(AdminUpdateRequestDTO request, Long id) {

        log.info("Updating Admin with ID: {}", id);

        User user = userValidator.validateUserExistence(id);

        customerService.handleUsername(request.getUsername(), user);
        customerService.handlePasswordUpdate(request, user);

        userRepository.save(user);

        log.info("Admin with ID {} updated successfully.", user.getUserId());
    }

    public void deleteAdmin(Long id) {
        User user = userValidator.validateUserExistence(id);
        if (!isAdmin(user)) {
            throw new IllegalArgumentException("You can only delete admins");
        }
        userRepository.delete(user);
    }

    public boolean isAdmin(User user) {
        return ADMIN.equals(user.getRole());
    }
}
