package com.vodafone.ecommerce;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.exception.QuantityExceedsStockException;
import com.vodafone.ecommerce.mapper.CartItemMapper;
import com.vodafone.ecommerce.model.dto.CartItemRequestDTO;
import com.vodafone.ecommerce.model.dto.CartItemResponseDTO;
import com.vodafone.ecommerce.model.entity.*;
import com.vodafone.ecommerce.model.enums.AccountStatus;
import com.vodafone.ecommerce.model.enums.Role;
import com.vodafone.ecommerce.repository.CartItemRepository;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.repository.ShoppingCartRepository;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.WishlistService;
import com.vodafone.ecommerce.service.impl.CartItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WishlistService wishlistService;
    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    private ShoppingCart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setup() {
        cart = new ShoppingCart();
        cart.setCartId(1L);

        product = new Product();
        product.setProductId(100L);
        product.setStockQuantity(10);

        cartItem = new CartItem();
        cartItem.setCartId(cart);
        cartItem.setProductId(product);
        cartItem.setQuantity(2);
    }

    @Test
    void addItemToCart_NewItem_Success() {
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(100L);
        request.setQuantity(2);

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(any())).thenReturn(Optional.empty());

        cartItemService.addItemToCart(1L, request);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_ExistingItem_MergedQuantity() {
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(100L);
        request.setQuantity(3);

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        CartItem existing = new CartItem(cart, product, 2, null, null);
        when(cartItemRepository.findById(any())).thenReturn(Optional.of(existing));

        cartItemService.addItemToCart(1L, request);

        verify(cartItemRepository).save(existing);
        assertEquals(5, existing.getQuantity());
    }

    @Test
    void addItemToCart_QuantityExceedsStock_ShouldThrow() {
        product.setStockQuantity(2);

        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(100L);
        request.setQuantity(5);

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(QuantityExceedsStockException.class, () ->
                cartItemService.addItemToCart(1L, request)
        );
    }

    @Test
    void removeCartItem_Success() {
        Long cartId = 1L;
        Long productId = 100L;
        CartItemPK key = new CartItemPK(cartId, productId);

        when(cartItemRepository.existsById(key)).thenReturn(true);

        cartItemService.removeCartItem(cartId, productId);

        verify(cartItemRepository).deleteById(key);
    }

    @Test
    void clearCart_Success() {
        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));

        cartItemService.clearCart(1L);

        verify(cartItemRepository).deleteAllByCartId(cart);
    }

    @Test
    void moveItemToWishlist_Success() {
        CartItemPK key = new CartItemPK(1L, 100L);
        when(cartItemRepository.existsById(key)).thenReturn(true);

        cartItemService.moveItemToWishlist(1L, 100L, 200L);

        verify(cartItemRepository).deleteById(key);
        verify(wishlistService).addToWishlist(200L, 100L);
    }

    @Test
    void moveFromWishlistToCart_Success() {
        cart.setCartId(1L);

        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(200L)).thenReturn(Optional.of(cart));
        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart)); // ADD THIS
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(any())).thenReturn(Optional.of(cartItem));
        when(cartItemMapper.toDTO(cartItem)).thenReturn(new CartItemResponseDTO());

        CartItemResponseDTO dto = cartItemService.moveFromWishlistToCart(200L, 100L, 2);

        assertNotNull(dto);
        verify(wishlistService).removeFromWishlist(200L, 100L);
    }

    @Test
    void addItemsToCartBulk_Success() {
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(100L);
        request.setQuantity(2);

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(any())).thenReturn(Optional.empty());

        cartItemService.addItemsToCartBulk(1L, List.of(request));

        verify(cartItemRepository).save(any());
    }

    @Test
    void updateCartItemQuantity_Success() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        CartItemRequestDTO dto = new CartItemRequestDTO();
        dto.setProductId(100L);
        dto.setQuantity(3);

        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(200L);
        User user = User.builder()
                .userId(1L)
                .username("jane_doe")
                .email("jane@example.com")
                .password("hashedpassword")
                .role(Role.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .customerProfile(profile)
                .build();
        when(userDetails.getUser()).thenReturn(user);

        cart.setCartItems(List.of(cartItem));
        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(200L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        cartItemService.updateCartItemQuantity(userDetails, dto);

        verify(cartItemRepository).save(cartItem);
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    void addItemsToCartBulk_EmptyList_ShouldNotFail() {
        cartItemService.addItemsToCartBulk(1L, List.of());
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    void addItemsToCartBulk_AllInvalid_ShouldSkip() {
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(100L);
        request.setQuantity(999); // Exceeds stock

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        product.setStockQuantity(1);

        cartItemService.addItemsToCartBulk(1L, List.of(request));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void updateCartItemQuantity_CartItemNotFound_ShouldThrow() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        CartItemRequestDTO dto = new CartItemRequestDTO();
        dto.setProductId(100L);
        dto.setQuantity(3);

        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(200L);
        when(userDetails.getUser()).thenReturn(new User(1L, "u", "p", "e", Role.CUSTOMER, AccountStatus.ACTIVE, profile));

        cart.setCartItems(List.of()); // no items
        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(200L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(NotFoundException.class, () -> cartItemService.updateCartItemQuantity(userDetails, dto));
    }

    @Test
    void getItemsByCartId_ShouldReturnMappedItems() {
        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart)).thenReturn(List.of(cartItem));
        when(cartItemMapper.toDTO(cartItem)).thenReturn(new CartItemResponseDTO());

        List<CartItemResponseDTO> result = cartItemService.getItemsByCartId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cartItemMapper).toDTO(cartItem);
    }

    @Test
    void getItemsByCartId_CartNotFound_ShouldThrow() {
        when(shoppingCartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartItemService.getItemsByCartId(99L));
    }

    @Test
    void updateCartItemQuantity_ProductNotFound_ShouldThrow() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        CartItemRequestDTO dto = new CartItemRequestDTO();
        dto.setProductId(100L);
        dto.setQuantity(3);

        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(200L);
        when(userDetails.getUser()).thenReturn(new User(1L, "u", "p", "e", Role.CUSTOMER, AccountStatus.ACTIVE, profile));

        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(200L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartItemService.updateCartItemQuantity(userDetails, dto));
    }

    @Test
    void addItemToCart_ExistingItemExceedsStock_ShouldThrow() {
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(100L);
        request.setQuantity(3); // Incoming request

        cart.setCartId(1L);
        product.setProductId(100L);
        product.setStockQuantity(5); // Only 5 in stock

        CartItem existingItem = new CartItem();
        existingItem.setCartId(cart);
        existingItem.setProductId(product);
        existingItem.setQuantity(4); // Already has 4 in cart

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(new CartItemPK(1L, 100L))).thenReturn(Optional.of(existingItem));

        assertThrows(QuantityExceedsStockException.class, () -> {
            cartItemService.addItemToCart(1L, request);
        });
    }

    @Test
    void removeCartItem_NotFound_ShouldThrow() {
        Long cartId = 1L;
        Long productId = 100L;
        CartItemPK key = new CartItemPK(cartId, productId);

        when(cartItemRepository.existsById(key)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> {
            cartItemService.removeCartItem(cartId, productId);
        });

        verify(cartItemRepository, never()).deleteById(any());
    }

    @Test
    void updateCartItemQuantity_ExceedsStock_ShouldThrow() {
        // Prepare DTO and User
        CartItemRequestDTO dto = new CartItemRequestDTO();
        dto.setProductId(100L);
        dto.setQuantity(5); // Request more than in stock

        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(200L);

        User user = new User();
        user.setCustomerProfile(profile);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(user);

        // Product has low stock
        product.setProductId(100L);
        product.setStockQuantity(2); // Only 2 in stock

        // Set up cart
        cart.setCartItems(List.of(cartItem));
        cartItem.setProductId(product); // Link product to cartItem

        when(shoppingCartRepository.findByCustomerProfile_CustomerProfileId(200L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(QuantityExceedsStockException.class, () ->
                cartItemService.updateCartItemQuantity(userDetails, dto)
        );
    }
}

