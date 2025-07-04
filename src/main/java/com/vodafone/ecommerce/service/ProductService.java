package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    void createProduct(ProductRequestDTO request);

    ProductResponseDetailsDTO findProductById(Long id);

    Page<ProductResponseDTO> findAllProducts(Pageable pageable);

    void updateProductById(Long id, ProductRequestDTO request);

    void rateProduct(Long productId, User user, RatingRequestDTO request);

    void addOrUpdateReview(Long productId, User user, ReviewRequestDTO reviewRequest);

    void deleteProductById(Long id);

    List<RatingResponseDTO> getRatingResponsesForProduct(Long productId);

    Page<ProductResponseDTO> findProductByCategory(Pageable pageable, String category);
}
