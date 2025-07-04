package com.vodafone.ecommerce;

import com.vodafone.ecommerce.mapper.ProductImageMapper;
import com.vodafone.ecommerce.model.dto.ProductImageRequestDTO;
import com.vodafone.ecommerce.model.dto.ProductImageResponseDTO;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.ProductImage;
import com.vodafone.ecommerce.repository.ProductImageRepository;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.service.impl.ProductImageServiceImpl;
import com.vodafone.ecommerce.validation.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductImageServiceImplTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private ProductImageMapper productImageMapper;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private Product product;
    private ProductImage image;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setProductId(1L);
        product.setProductImages(new ArrayList<>());

        image = new ProductImage();
        image.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    void insertNewImage_ShouldSaveImageAndReturnDTO() {
        ProductImageRequestDTO request = new ProductImageRequestDTO();
        request.setImageUrl("http://example.com/image.jpg");

        ProductImageResponseDTO responseDTO = new ProductImageResponseDTO();
        responseDTO.setImageUrl("http://example.com/image.jpg");

        when(productValidator.validateProductExistence(1L)).thenReturn(product);
        when(productImageMapper.mapToProductImage(request)).thenReturn(image);
        when(productImageMapper.mapToProductImageResponseDTO(image)).thenReturn(responseDTO);

        ProductImageResponseDTO result = productImageService.insertNewImage(1L, request);

        verify(productRepository).save(product);
        assertEquals("http://example.com/image.jpg", result.getImageUrl());
    }

    @Test
    void findImagesByProductId_ShouldReturnMappedImages() {
        List<ProductImage> productImages = List.of(image);
        ProductImageResponseDTO responseDTO = new ProductImageResponseDTO();
        responseDTO.setImageUrl("http://example.com/image.jpg");

        when(productValidator.validateProductImagesExistence(1L)).thenReturn(productImages);
        when(productImageMapper.mapToProductImageResponseDTO(image)).thenReturn(responseDTO);

        List<ProductImageResponseDTO> result = productImageService.findImagesByProductId(1L);

        assertEquals(1, result.size());
        assertEquals("http://example.com/image.jpg", result.get(0).getImageUrl());
    }

    @Test
    void deleteImageByProductId_ShouldRemoveImageAndDelete() {
        product.getProductImages().add(image); // index 0, displayOrder = 1

        when(productValidator.validateProductExistence(1L)).thenReturn(product);

        productImageService.deleteImageByProductId(1L, 1);

        assertTrue(product.getProductImages().isEmpty());
        verify(productImageRepository).delete(image);
    }
}

