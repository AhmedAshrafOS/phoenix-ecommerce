package com.vodafone.ecommerce;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.mapper.WishlistMapper;
import com.vodafone.ecommerce.model.dto.WishlistResponseDTO;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.WishList;
import com.vodafone.ecommerce.repository.CustomerProfileRepository;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.repository.WishListRepository;
import com.vodafone.ecommerce.service.impl.WishlistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistServiceImplTest {

    private WishListRepository wishListRepository;
    private ProductRepository productRepository;
    private WishlistMapper wishlistMapper;
    private CustomerProfileRepository customerProfileRepository;

    private WishlistServiceImpl wishlistService;

    private CustomerProfile customer;
    private Product product;
    private WishList wishList;

    @BeforeEach
    void setUp() {
        wishListRepository = mock(WishListRepository.class);
        productRepository = mock(ProductRepository.class);
        wishlistMapper = mock(WishlistMapper.class);
        customerProfileRepository = mock(CustomerProfileRepository.class);

        wishlistService = new WishlistServiceImpl(wishListRepository, productRepository, wishlistMapper, customerProfileRepository);

        customer = new CustomerProfile();
        customer.setCustomerProfileId(1L);

        product = new Product();
        product.setProductId(100L);

        wishList = new WishList();
        wishList.setCustomerProfileId(customer);
        wishList.setProductId(product);
    }

    @Test
    void getWishlistByCustomerId_ShouldReturnList() {
        when(wishListRepository.findByCustomerProfileIdCustomerProfileId(1L))
                .thenReturn(List.of(wishList));

        WishlistResponseDTO dto = new WishlistResponseDTO();
        when(wishlistMapper.toDTO(wishList)).thenReturn(dto);

        List<WishlistResponseDTO> result = wishlistService.getWishlistByCustomerId(1L);

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(wishlistMapper).toDTO(wishList);
    }

    @Test
    void addToWishlist_Success() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(wishListRepository.existsByCustomerProfileIdAndProductId(customer, product)).thenReturn(false);

        wishlistService.addToWishlist(1L, 100L);

        verify(wishListRepository).save(any(WishList.class));
    }

    @Test
    void addToWishlist_ProductAlreadyExists_ShouldNotSaveAgain() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(wishListRepository.existsByCustomerProfileIdAndProductId(customer, product)).thenReturn(true);

        wishlistService.addToWishlist(1L, 100L);

        verify(wishListRepository, never()).save(any());
    }

    @Test
    void addToWishlist_CustomerNotFound_ShouldThrow() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> wishlistService.addToWishlist(1L, 100L));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void addToWishlist_ProductNotFound_ShouldThrow() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(100L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> wishlistService.addToWishlist(1L, 100L));

        assertEquals("Product not found with ID: 100", exception.getMessage());
    }

    @Test
    void removeFromWishlist_ShouldDelete() {
        wishlistService.removeFromWishlist(1L, 100L);

        verify(wishListRepository).deleteByCustomerProfileIdAndProductId(1L, 100L);
    }
}

