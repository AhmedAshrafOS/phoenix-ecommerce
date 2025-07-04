package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.ProductImageRequestDTO;
import com.vodafone.ecommerce.model.dto.ProductImageResponseDTO;

import java.util.List;

public interface ProductImageService {

    ProductImageResponseDTO insertNewImage(Long productId, ProductImageRequestDTO request);

    List<ProductImageResponseDTO> findImagesByProductId(Long productId);

    void deleteImageByProductId(Long productId, int displayOrder);
}
