package com.vodafone.ecommerce.validation;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.ProductImage;
import com.vodafone.ecommerce.repository.ProductImageRepository;
import com.vodafone.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public Product validateProductExistence(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new NotFoundException("The Product with Id:[%s] doesn't exist.".formatted(productId)));
    }

    public List<ProductImage> validateProductImagesExistence(Long id) {
        List<ProductImage> images = productImageRepository.findProductImageByProductProductId(id);
        if (images.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Images with Product Id:[%s] doesn't exist.".formatted(id));
        return images;
    }
}
