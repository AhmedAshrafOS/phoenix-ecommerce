package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.WishlistResponseDTO;

import java.util.List;

public interface WishlistService {

    List<WishlistResponseDTO> getWishlistByCustomerId(Long customerProfileId);

    void addToWishlist(Long customerProfileId, Long productId);

    void removeFromWishlist(Long customerProfileId, Long productId);
}
