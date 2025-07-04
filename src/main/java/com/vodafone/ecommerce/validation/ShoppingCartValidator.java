package com.vodafone.ecommerce.validation;

import com.vodafone.ecommerce.exception.EmptyCartException;
import com.vodafone.ecommerce.model.entity.ShoppingCart;
import com.vodafone.ecommerce.repository.CartItemRepository;
import com.vodafone.ecommerce.repository.ShoppingCartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShoppingCartValidator {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Ensures a cart exists for the given customer profile.
     *
     * @param customerProfileId id of the profile owning the cart
     * @return the cart entity (never {@code null})
     * @throws ResponseStatusException 404 if not found
     */
    public ShoppingCart requireCart(Long customerProfileId) {
        return shoppingCartRepository
                .findByCustomerProfile_CustomerProfileId(customerProfileId)
                .orElseThrow(() -> notFound("Shopping cart", customerProfileId));
    }

    /**
     * Ensures the cart is not empty.
     *
     * @param cart previously validated cart
     * @throws ResponseStatusException 400 if empty
     */
    public void requireNotEmpty(ShoppingCart cart) {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.warn("Cart {} is empty", cart.getCartId());
            throw new EmptyCartException("Cart must contain at least one item");

        }
    }

    /**
     * Check for the cart if it exits and then clear it after ensure the order has been saved in the database.
     *
     * @param cart the cart's user
     */
    @Transactional
    public void clearCart(ShoppingCart cart) {
        ShoppingCart tempCart = shoppingCartRepository.findById(cart.getCartId())
                .orElseThrow(() -> notFound("cart", cart.getCartId()));
        cartItemRepository.deleteAllByCartId(tempCart);
        log.info("Cleared all items from cart {}", tempCart.getCartId());
    }

    private ResponseStatusException notFound(String message, Long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                String.format("%s not found for id: %d", message, id)
        );
    }
}
