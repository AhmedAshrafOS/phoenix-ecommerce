package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.CustomerService;
import com.vodafone.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CustomerService customerService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void addAdmin(@Valid @RequestBody AdminRequestDTO request) {
        userService.addAdmin(request);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "/admins", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AdminResponseDTO>> findAllAdmins() {
        return ResponseEntity.ok(userService.getAllAdmins());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "/admin-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminResponseDTO> findLoggedInAdminProfile() {
        return ResponseEntity.ok(userService.getAdminProfile());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerProfileResponseDTO> findCustomerProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(customerService.getCustomerProfile(user));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAdmin(@Valid @RequestBody AdminUpdateRequestDTO updateRequest, @PathVariable Long id) {
        userService.updateAdmin(updateRequest, id);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateCustomer(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody CustomerUpdateRequestDTO updateRequest) {
        User user = userDetails.getUser();
        customerService.updateCustomerProfile(updateRequest, user);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdmin(@PathVariable Long id) {
        userService.deleteAdmin(id);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping(path = "/customer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        userService.deleteUser(user);
    }
}
