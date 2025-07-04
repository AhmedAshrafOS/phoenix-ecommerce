package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.exception.QuantityExceedsStockException;
import com.vodafone.ecommerce.mapper.CartItemMapper;
import com.vodafone.ecommerce.model.dto.CartItemRequestDTO;
import com.vodafone.ecommerce.model.dto.CartItemResponseDTO;
import com.vodafone.ecommerce.model.entity.CartItem;
import com.vodafone.ecommerce.model.entity.CartItemPK;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.ShoppingCart;
import com.vodafone.ecommerce.repository.CartItemRepository;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.repository.ShoppingCartRepository;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.CartItemService;
import com.vodafone.ecommerce.service.WishlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private static final String NOT_FOUND = " not found";
    private final CartItemRepository cartItemRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductRepository productRepository;
    private final WishlistService wishlistService;
    private final CartItemMapper cartItemMapper;

    public void addItemToCart(Long cartId, CartItemRequestDTO request) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product with ID " + request.getProductId() + " not found"));

        int requestedQuantity = request.getQuantity();

        if (requestedQuantity > product.getStockQuantity()) {
            throw new QuantityExceedsStockException("Requested quantity (" + requestedQuantity +
                    ") exceeds available stock (" + product.getStockQuantity()+ ")");
        }

        CartItemPK key = new CartItemPK(cartId, request.getProductId());
        CartItem existing = cartItemRepository.findById(key).orElse(null);

        if (existing != null) {
            int newQuantity = existing.getQuantity() + requestedQuantity;
            if (newQuantity > product.getStockQuantity()) {
                throw new QuantityExceedsStockException("Total quantity (" + newQuantity +
                        ") exceeds available stock (" + product.getStockQuantity() + ")");
            }
            existing.setQuantity(newQuantity);
            cartItemRepository.save(existing);
            log.info("Updated quantity for product {} in cart {}", request.getProductId(), cartId);
        } else {
            CartItem item = CartItem.builder()
                    .cartId(cart)
                    .productId(product)
                    .quantity(requestedQuantity)
                    .build();
            cartItemRepository.save(item);
            log.info("Added new product {} to cart {}", request.getProductId(), cartId);
        }
    }

    public List<CartItemResponseDTO> getItemsByCartId(Long cartId) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart with ID " + cartId + NOT_FOUND));

        List<CartItem> items = cartItemRepository.findByCartId(cart);

        return items.stream()
                .map(cartItemMapper::toDTO)
                .toList();
    }

    public void removeCartItem(Long cartId, Long productId) {
        CartItemPK key = new CartItemPK(cartId, productId);
        if (!cartItemRepository.existsById(key)) {
            throw new NotFoundException("Cart item not found for cartId=" + cartId + " and productId=" + productId);
        }
        cartItemRepository.deleteById(key);
        log.info("Removed product {} from cart {}", productId, cartId);
    }

    @Transactional
    public void clearCart(Long cartId) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart with ID " + cartId + NOT_FOUND));
        cartItemRepository.deleteAllByCartId(cart);
        log.info("Cleared all items from cart {}", cartId);
    }

    public void moveItemToWishlist(Long cartId, Long productId, Long customerProfileId) {
        removeCartItem(cartId, productId);

        wishlistService.addToWishlist(customerProfileId, productId);
        log.info("Moved product {} from cart {} to wishlist of customer {}", productId, cartId, customerProfileId);
    }

    public CartItemResponseDTO moveFromWishlistToCart(Long customerProfileId, Long productId, int quantity) {
        wishlistService.removeFromWishlist(customerProfileId, productId);

        ShoppingCart cart = shoppingCartRepository.findByCustomerProfile_CustomerProfileId(customerProfileId)
                .orElseThrow(() -> new NotFoundException("Cart not found for customer ID " + customerProfileId));

        Long cartId = cart.getCartId();

        CartItemRequestDTO requestDTO = new CartItemRequestDTO();
        requestDTO.setProductId(productId);
        requestDTO.setQuantity(quantity);
        addItemToCart(cartId, requestDTO);

        CartItemPK key = new CartItemPK(cartId, productId);
        CartItem savedItem = cartItemRepository.findById(key)
                .orElseThrow(() -> new NotFoundException("Cart item not found after adding"));

        return cartItemMapper.toDTO(savedItem);
    }

    public void addItemsToCartBulk(Long cartId, List<CartItemRequestDTO> items) {
        for (CartItemRequestDTO request : items) {
            try {
                addItemToCart(cartId, request);
            } catch (IllegalArgumentException | NotFoundException | QuantityExceedsStockException e) {
                log.warn("Skipping item with product ID {}: {}", request.getProductId(), e.getMessage());
            }
        }
    }


    public void updateCartItemQuantity(CustomUserDetails userDetails, CartItemRequestDTO cartItemRequestDTO) {
        Long customerId = userDetails.getUser().getCustomerProfile().getCustomerProfileId();
        int quantity = cartItemRequestDTO.getQuantity();

        ShoppingCart customerCart = shoppingCartRepository.findByCustomerProfile_CustomerProfileId(customerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for customer ID "));

        Product product = productRepository.findById(cartItemRequestDTO.getProductId())
                .orElseThrow(() -> new NotFoundException("Product ID " + cartItemRequestDTO.getProductId() + " not found"));

        if (quantity > product.getStockQuantity()) {
            throw new QuantityExceedsStockException("Requested quantity (" + quantity +
                    ") exceeds available stock (" + product.getStockQuantity() + ") for product ID " + product.getProductId());
        }
        CartItem cartItemToBeUpdated = customerCart.getCartItems().stream()
                .filter(item -> item.getProductId().getProductId().equals(cartItemRequestDTO.getProductId()))
                .findFirst().orElseThrow(() -> new NotFoundException("Product ID " + cartItemRequestDTO.getProductId() + " not found"));

        cartItemToBeUpdated.setQuantity(quantity);

        cartItemRepository.save(cartItemToBeUpdated);
    }
}
