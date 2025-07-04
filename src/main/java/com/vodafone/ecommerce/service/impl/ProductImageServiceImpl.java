package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.mapper.ProductImageMapper;
import com.vodafone.ecommerce.model.dto.ProductImageRequestDTO;
import com.vodafone.ecommerce.model.dto.ProductImageResponseDTO;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.ProductImage;
import com.vodafone.ecommerce.repository.ProductImageRepository;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.service.ProductImageService;
import com.vodafone.ecommerce.validation.ProductValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductImageMapper productImageMapper;
    private final ProductValidator productValidator;
    private final ProductRepository productRepository;

    public ProductImageResponseDTO insertNewImage(Long productId, ProductImageRequestDTO request) {

        Product product = productValidator.validateProductExistence(productId);

        ProductImage image = productImageMapper.mapToProductImage(request);
        image.setProduct(product);
        product.getProductImages().add(image);

        productRepository.save(product);

        return productImageMapper.mapToProductImageResponseDTO(image);
    }

    public List<ProductImageResponseDTO> findImagesByProductId(Long productId) {
        List<ProductImage> productImages = productValidator.validateProductImagesExistence(productId);
        return productImages
                .stream()
                .map(productImageMapper::mapToProductImageResponseDTO)
                .toList();
    }

    public void deleteImageByProductId(Long productId, int displayOrder) {
        int imageIndex = displayOrder - 1;
        Product product = productValidator.validateProductExistence(productId);
        ProductImage image = product.getProductImages().get(imageIndex);

        product.getProductImages().remove(imageIndex);

        productImageRepository.delete(image);
    }
}
