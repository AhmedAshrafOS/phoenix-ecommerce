package com.vodafone.ecommerce;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.mapper.CartItemMapper;
import com.vodafone.ecommerce.model.dto.CartItemResponseDTO;
import com.vodafone.ecommerce.model.dto.CartResponseDTO;
import com.vodafone.ecommerce.model.entity.CartItem;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.ShoppingCart;
import com.vodafone.ecommerce.repository.ShoppingCartRepository;
import com.vodafone.ecommerce.service.impl.ShoppingCartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    private CustomerProfile customerProfile;
    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {
        customerProfile = new CustomerProfile();
        customerProfile.setCustomerProfileId(1L);

        shoppingCart = new ShoppingCart();
        shoppingCart.setCartId(10L);
        shoppingCart.setCustomerProfile(customerProfile);
    }

    @Test
    void testCreateCartForCustomerProfile_Success() {
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(shoppingCart);

        assertDoesNotThrow(() -> shoppingCartService.createCartForCustomerProfile(customerProfile));

        verify(shoppingCartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void testGetCartIdByCustomerId_Success() {
        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(1L))
                .thenReturn(Optional.of(shoppingCart));

        Long cartId = shoppingCartService.getCartIdByCustomerId(1L);

        assertEquals(10L, cartId);
        verify(shoppingCartRepository).findByCustomerProfile_CustomerProfileId(1L);
    }

    @Test
    void testGetCartIdByCustomerId_NotFound() {
        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(2L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                shoppingCartService.getCartIdByCustomerId(2L));

        assertEquals("Cart not found for customer ID: 2", exception.getMessage());
    }

    @Test
    void testGetCartByCustomerId_Success() {
        Product product = new Product();
        product.setProductId(101L);

        ShoppingCart cart = new ShoppingCart();
        cart.setCartId(1L);

        CartItem cartItem = new CartItem();
        cartItem.setProductId(product);
        cartItem.setCartId(cart);
        cartItem.setQuantity(2);

        cart.setCartItems(List.of(cartItem)); // reuse the same cart

        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setProductId(101L);

        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(1L))
                .thenReturn(Optional.of(cart));
        when(cartItemMapper.toDTO(cartItem)).thenReturn(dto);

        CartResponseDTO response = shoppingCartService.getCartByCustomerId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getCartId());
        assertEquals(1, response.getCartItems().size());
        assertEquals(101L, response.getCartItems().get(0).getProductId());

        verify(shoppingCartRepository).findByCustomerProfile_CustomerProfileId(1L);
        verify(cartItemMapper).toDTO(cartItem);
    }



    @Test
    void testGetCartByCustomerId_NotFound() {
        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(3L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                shoppingCartService.getCartByCustomerId(3L));

        assertEquals("Cart not found for customer ID: 3", exception.getMessage());
    }
}

