package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.mapper.WishlistMapper;
import com.vodafone.ecommerce.model.dto.WishlistResponseDTO;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.WishList;
import com.vodafone.ecommerce.repository.CustomerProfileRepository;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.repository.WishListRepository;
import com.vodafone.ecommerce.service.WishlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishListRepository wishListRepository;
    private final ProductRepository productRepository;
    private final WishlistMapper wishlistMapper;
    private final CustomerProfileRepository customerProfileRepository;

    public List<WishlistResponseDTO> getWishlistByCustomerId(Long customerProfileId) {
        List<WishList> items = wishListRepository.findByCustomerProfileIdCustomerProfileId(customerProfileId);
        return items.stream()
                .map(wishlistMapper::toDTO)
                .toList();
    }

    @Transactional
    public void addToWishlist(Long customerProfileId, Long productId) {
        CustomerProfile customer = customerProfileRepository.findById(customerProfileId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));

        if (wishListRepository.existsByCustomerProfileIdAndProductId(customer, product)) {
            log.info("Product already in wishlist for customer {}", customerProfileId);
            return;
        }

        WishList wishList = WishList.builder()
                .customerProfileId(customer)
                .productId(product)
                .build();

        wishListRepository.save(wishList);
        log.info("Added product {} to wishlist for customer {}", productId, customerProfileId);
    }

    @Transactional
    public void removeFromWishlist(Long customerProfileId, Long productId) {
        wishListRepository.deleteByCustomerProfileIdAndProductId(customerProfileId, productId);
    }
}
