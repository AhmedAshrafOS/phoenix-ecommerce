package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.CartItemRequestDTO;
import com.vodafone.ecommerce.model.dto.CartItemResponseDTO;
import com.vodafone.ecommerce.security.model.CustomUserDetails;

import java.util.List;

public interface CartItemService {

    void addItemToCart(Long cartId, CartItemRequestDTO request);

    List<CartItemResponseDTO> getItemsByCartId(Long cartId);

    void removeCartItem(Long cartId, Long productId);

    void clearCart(Long cartId);

    void moveItemToWishlist(Long cartId, Long productId, Long customerProfileId);

    CartItemResponseDTO moveFromWishlistToCart(Long customerProfileId, Long productId, int quantity);

    void updateCartItemQuantity(CustomUserDetails userDetails, CartItemRequestDTO cartItemRequestDTO);

    void addItemsToCartBulk(Long cartId, List<CartItemRequestDTO> items);
}
