package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.CartResponseDTO;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartResponseDTO> findCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        CartResponseDTO cart = shoppingCartService.getCartByCustomerId(customerId);
        return ResponseEntity.ok(cart);
    }
}
