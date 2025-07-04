package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.CartItemRequestDTO;
import com.vodafone.ecommerce.model.dto.CartItemResponseDTO;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.CartItemService;
import com.vodafone.ecommerce.service.ShoppingCartService;
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
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/cartItems")
public class CartItemController {

    private final CartItemService cartItemService;
    private final ShoppingCartService shoppingCartService;

    @PostMapping(path = "/item", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void addCartItem(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody CartItemRequestDTO request) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        Long cartId = shoppingCartService.getCartIdByCustomerId(customerId);
        cartItemService.addItemToCart(cartId, request);
    }

    @PostMapping(path = "/items", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void addCartItems(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody List<CartItemRequestDTO> requestItems) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        Long cartId = shoppingCartService.getCartIdByCustomerId(customerId);
        cartItemService.addItemsToCartBulk(cartId, requestItems);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CartItemResponseDTO>> getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        Long cartId = shoppingCartService.getCartIdByCustomerId(customerId);
        return ResponseEntity.ok(cartItemService.getItemsByCartId(cartId));
    }

    @PatchMapping(path = "/item", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void updateCartItemQuantity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody @Valid CartItemRequestDTO cartItemRequestDTO) {
        cartItemService.updateCartItemQuantity(userDetails, cartItemRequestDTO);
    }

    @DeleteMapping(path = "/item/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void deleteCartItem(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long productId) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        Long cartId = shoppingCartService.getCartIdByCustomerId(customerId);
        cartItemService.removeCartItem(cartId, productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void deleteCartItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        Long cartId = shoppingCartService.getCartIdByCustomerId(customerId);
        cartItemService.clearCart(cartId);
    }

    @PostMapping(path = "/item/toWishlist/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void moveCartItemToWishlist(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long productId) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        Long cartId = shoppingCartService.getCartIdByCustomerId(customerId);
        cartItemService.moveItemToWishlist(cartId, productId, customerId);
    }
}
