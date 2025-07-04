package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.CartItemResponseDTO;
import com.vodafone.ecommerce.model.dto.WishlistResponseDTO;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.CartItemService;
import com.vodafone.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final CartItemService cartItemService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WishlistResponseDTO>> getWishlist(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        List<WishlistResponseDTO> wishlist = wishlistService.getWishlistByCustomerId(customerId);
        return ResponseEntity.ok(wishlist);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping(path = "/product/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addToWishlist(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Long productId) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        wishlistService.addToWishlist(customerId, productId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping(path = "/product/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFromWishlist(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @PathVariable Long productId) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        wishlistService.removeFromWishlist(customerId, productId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping(path = "/product/toCart/{productId}")
    public ResponseEntity<CartItemResponseDTO> moveFromWishlistToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                      @PathVariable Long productId,
                                                                      @RequestParam(defaultValue = "1") int quantity) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        CartItemResponseDTO response = cartItemService.moveFromWishlistToCart(customerId, productId, quantity);
        return ResponseEntity.ok(response);
    }
}
