package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.mapper.CartItemMapper;
import com.vodafone.ecommerce.model.dto.CartItemResponseDTO;
import com.vodafone.ecommerce.model.dto.CartResponseDTO;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.ShoppingCart;
import com.vodafone.ecommerce.repository.ShoppingCartRepository;
import com.vodafone.ecommerce.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemMapper cartItemMapper;

    public void createCartForCustomerProfile(CustomerProfile customerProfile) {
        log.info("Creating shopping cart for CustomerProfile ID: {}", customerProfile.getCustomerProfileId());

        ShoppingCart cart = new ShoppingCart();
        cart.setCustomerProfile(customerProfile);
        cart.setCartItems(List.of());

        ShoppingCart savedCart = shoppingCartRepository.save(cart);

        log.info("Shopping cart created with ID: {} for CustomerProfile ID: {}",
                savedCart.getCartId(), customerProfile.getCustomerProfileId());
    }

    public Long getCartIdByCustomerId(Long customerId) {
        return shoppingCartRepository.findByCustomerProfile_CustomerProfileId(customerId)
                .map(ShoppingCart::getCartId)
                .orElseThrow(() ->
                        new NotFoundException("Cart not found for customer ID: " + customerId));
    }

    public CartResponseDTO getCartByCustomerId(Long customerId) {
        ShoppingCart cart = shoppingCartRepository.findByCustomerProfile_CustomerProfileId(customerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for customer ID: " + customerId));

        List<CartItemResponseDTO> items = cart.getCartItems().stream()
                .map(cartItemMapper::toDTO)
                .toList();

        CartResponseDTO response = new CartResponseDTO();
        response.setCartId(cart.getCartId());
        response.setCartItems(items);
        return response;
    }
}
