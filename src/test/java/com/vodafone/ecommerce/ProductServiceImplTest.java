package com.vodafone.ecommerce;

import com.cloudinary.Cloudinary;
import com.vodafone.ecommerce.exception.ForbiddenActionException;
import com.vodafone.ecommerce.mapper.ProductMapper;
import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.*;
import com.vodafone.ecommerce.model.enums.Category;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.repository.RatingRepository;
import com.vodafone.ecommerce.validation.OrderValidator;
import com.vodafone.ecommerce.validation.ProductValidator;
import com.vodafone.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock private Cloudinary cloudinary;
    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;
    @Mock private RatingRepository ratingRepository;
    @Mock private ProductValidator productValidator;
    @Mock private OrderValidator orderValidator;
    @Mock private MultipartFile multipartFile;
    @InjectMocks private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindProductById_ShouldReturnProductDetails() {
        Product product = new Product();
        product.setProductId(1L);
        product.setPrice(100.0);
        product.setDiscountPercentage(10.0);

        when(productValidator.validateProductExistence(1L)).thenReturn(product);

        ProductResponseDetailsDTO dto = new ProductResponseDetailsDTO();
        when(productMapper.mapToProductResponseDetailsDTO(product)).thenReturn(dto);

        ProductResponseDetailsDTO result = productService.findProductById(1L);

        assertEquals(product.getFinalUnitPrice(), result.getTotalFinalPrice());
    }

    @Test
    void testFindAllProducts_ShouldReturnPagedList() {
        Product product = new Product();
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(productMapper.mapToProductResponseDTO(product)).thenReturn(new ProductResponseDTO());

        Page<ProductResponseDTO> result = productService.findAllProducts(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testRateProduct_NewRating_ShouldCreateAndUpdateStats() {
        Product product = new Product();
        product.setReviewCount(0);
        product.setAverageRating(0.0);

        User user = new User();
        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(1L);
        user.setCustomerProfile(profile);

        Order order = new Order();
        order.setCustomerProfile(profile);

        RatingRequestDTO dto = new RatingRequestDTO();
        dto.setRatingValue(4);
        dto.setOrderId(1L);

        when(productValidator.validateProductExistence(1L)).thenReturn(product);
        when(orderValidator.requireExistingOrder(1L)).thenReturn(order);
        when(ratingRepository.findByCustomerProfileAndProductAndOrder(profile, product, order))
                .thenReturn(Optional.empty());

        productService.rateProduct(1L, user, dto);

        verify(ratingRepository).save(any(Rating.class));
        verify(productRepository).save(product);
    }

    @Test
    void testRateProduct_ExistingSameRating_ShouldDoNothing() {
        Product product = new Product();
        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(1L);
        User user = new User();
        user.setCustomerProfile(profile);

        Order order = new Order();
        order.setCustomerProfile(profile);

        Rating rating = new Rating();
        rating.setRatingValue(5);

        RatingRequestDTO dto = new RatingRequestDTO();
        dto.setOrderId(1L);
        dto.setRatingValue(5);

        when(productValidator.validateProductExistence(1L)).thenReturn(product);
        when(orderValidator.requireExistingOrder(1L)).thenReturn(order);
        when(ratingRepository.findByCustomerProfileAndProductAndOrder(profile, product, order))
                .thenReturn(Optional.of(rating));

        productService.rateProduct(1L, user, dto);

        verify(ratingRepository, never()).save(any());
    }
    @Test
    void testAddOrUpdateReview_WithoutExistingRating_ShouldThrow() {
        Product product = new Product();
        User user = new User();
        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(1L);
        user.setCustomerProfile(profile);
        Order order = new Order();

        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setOrderId(1L);
        dto.setComment("Nice!");

        when(productValidator.validateProductExistence(1L)).thenReturn(product);
        when(orderValidator.requireExistingOrder(1L)).thenReturn(order);
        when(ratingRepository.findByCustomerProfileAndProductAndOrder(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.addOrUpdateReview(1L, user, dto));
    }

    @Test
    void testDeleteProductById_ShouldDelete() {
        Product product = new Product();
        when(productValidator.validateProductExistence(5L)).thenReturn(product);

        productService.deleteProductById(5L);

        verify(productRepository).delete(product);
    }

    @Test
    void testFindProductByCategory_ValidCategory_ShouldReturnPage() {
        Product product = new Product();
        product.setCategory(Category.LAPTOPS);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findByCategory(Category.LAPTOPS, pageable)).thenReturn(productPage);
        when(productMapper.mapToProductResponseDTO(any())).thenReturn(new ProductResponseDTO());

        Page<ProductResponseDTO> result = productService.findProductByCategory(pageable, "LAPTOPS");

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindProductByCategory_InvalidCategory_ShouldThrow() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(IllegalArgumentException.class, () -> {
            productService.findProductByCategory(pageable, "INVALID_CAT");
        });
    }
    @Test
    void testCreateProduct_WithCloudinaryUpload() throws Exception {
        // Arrange
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("iPhone");
        request.setImagesUrls(List.of(multipartFile));

        Product product = new Product();
        product.setProductId(1L);

        when(productRepository.existsByName("iPhone")).thenReturn(false);
        when(productMapper.mapToProduct(request)).thenReturn(product);
        when(multipartFile.getBytes()).thenReturn("fake-image-bytes".getBytes());

        Map<String, Object> uploadResult = Map.of("secure_url", "https://cloudinary.com/fake-image.jpg");

        var uploader = mock(com.cloudinary.Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);
        when(productRepository.save(product)).thenReturn(product);

        // Act
        productService.createProduct(request);

        // Assert
        verify(productRepository).save(product);
        assertEquals(1, product.getProductImages().size());
        assertEquals("https://cloudinary.com/fake-image.jpg", product.getProductImages().get(0).getImageUrl());
    }

    @Test
    void testCreateProduct_DuplicateName_ShouldThrowException() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("iPhone");

        when(productRepository.existsByName("iPhone")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));
    }
    @Test
    void testUpdateProductById_ShouldUpdateFieldsAndSave() {
        // Arrange
        Long productId = 1L;
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Updated Name");
        request.setFeatures("Updated Features");
        request.setSpecs("Updated Specs");
        request.setPrice(999.99);
        request.setStockQuantity(10);
        request.setLowStockThreshold(2);
        request.setBrandName("Updated Brand");
        request.setCategory(Category.WATCHES);
        request.setDiscountPercentage(15.0);

        Product existingProduct = new Product();
        existingProduct.setProductId(productId);

        when(productValidator.validateProductExistence(productId)).thenReturn(existingProduct);
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        // Act
        productService.updateProductById(productId, request);

        // Assert
        verify(productValidator).validateProductExistence(productId);
        verify(productRepository).save(existingProduct);

        assertEquals("Updated Name", existingProduct.getName());
        assertEquals("Updated Features", existingProduct.getFeatures());
        assertEquals("Updated Specs", existingProduct.getSpecs());
        assertEquals(999.99, existingProduct.getPrice());
        assertEquals(10, existingProduct.getStockQuantity());
        assertEquals(2, existingProduct.getLowStockThreshold());
        assertEquals("Updated Brand", existingProduct.getBrandName());
        assertEquals(Category.WATCHES, existingProduct.getCategory());
        assertEquals(15.0, existingProduct.getDiscountPercentage());
    }
    @Test
    void testGetRatingResponsesForProduct_ShouldReturnMappedDTOs() {
        // Arrange
        Long productId = 1L;

        Rating rating1 = new Rating();
        CustomerProfile profile1 = new CustomerProfile();
        profile1.setFirstName("John");
        profile1.setLastName("Doe");
        rating1.setCustomerProfile(profile1);
        rating1.setComment("Great product!");

        Rating rating2 = new Rating();
        CustomerProfile profile2 = new CustomerProfile();
        profile2.setFirstName("Jane");
        profile2.setLastName("Smith");
        rating2.setCustomerProfile(profile2);
        rating2.setComment("Could be better");

        List<Rating> ratings = List.of(rating1, rating2);

        when(ratingRepository.findByProduct_ProductId(productId)).thenReturn(ratings);

        // Act
        List<RatingResponseDTO> result = productService.getRatingResponsesForProduct(productId);

        // Assert
        assertEquals(2, result.size());

        assertEquals("Great product!", result.get(0).getComment());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());

        assertEquals("Could be better", result.get(1).getComment());
        assertEquals("Jane", result.get(1).getFirstName());
        assertEquals("Smith", result.get(1).getLastName());

        verify(ratingRepository).findByProduct_ProductId(productId);
    }
    @Test
    void testRateProduct_UpdatesExistingRatingAndStats() {
        // Arrange
        Long productId = 1L;
        Long orderId = 10L;

        Product product = new Product();
        product.setProductId(productId);
        product.setAverageRating(4.0);  // avg of 1 rating
        product.setReviewCount(1);      // 1 existing review

        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(101L);

        User user = new User();
        user.setUsername("testUser");
        user.setCustomerProfile(profile);

        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerProfile(profile);

        Rating existingRating = new Rating();
        existingRating.setRatingValue(4); // old
        existingRating.setCustomerProfile(profile);
        existingRating.setProduct(product);
        existingRating.setOrder(order);

        RatingRequestDTO request = new RatingRequestDTO();
        request.setOrderId(orderId);
        request.setRatingValue(5); // new

        when(productValidator.validateProductExistence(productId)).thenReturn(product);
        when(orderValidator.requireExistingOrder(orderId)).thenReturn(order);
        when(ratingRepository.findByCustomerProfileAndProductAndOrder(profile, product, order)).thenReturn(Optional.of(existingRating));

        // Act
        productService.rateProduct(productId, user, request);

        // Assert
        assertEquals(5.0, product.getAverageRating()); // updated from 4 → 5
        verify(productRepository).save(product);       // confirms stats update persisted
        verify(ratingRepository).save(existingRating); // confirms rating updated
    }
    @Test
    void testRateProduct_ThrowsForbiddenActionException_WhenOrderNotBelongToUser() {
        // Arrange
        Long productId = 1L;
        Long orderId = 100L;

        Product product = new Product();
        product.setProductId(productId);

        CustomerProfile userProfile = new CustomerProfile();
        userProfile.setCustomerProfileId(1L);

        CustomerProfile orderProfile = new CustomerProfile();
        orderProfile.setCustomerProfileId(2L); // Different from userProfile

        User user = new User();
        user.setUsername("testUser");
        user.setCustomerProfile(userProfile);

        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerProfile(orderProfile); // Belongs to different customer

        RatingRequestDTO request = new RatingRequestDTO();
        request.setOrderId(orderId);
        request.setRatingValue(5);

        when(productValidator.validateProductExistence(productId)).thenReturn(product);
        when(orderValidator.requireExistingOrder(orderId)).thenReturn(order);

        // Act & Assert
        ForbiddenActionException exception = assertThrows(ForbiddenActionException.class, () -> {
            productService.rateProduct(productId, user, request);
        });

        assertEquals("You cannot rate a product from an order that does not belong to you.", exception.getMessage());

        // Optionally verify that no rating repository calls happened
        verify(ratingRepository, never()).save(any());
    }
    @Test
    void testAddOrUpdateReview_SuccessfullyUpdatesReview() {
        // Arrange
        Long productId = 1L;
        Long orderId = 100L;

        Product product = new Product();
        product.setProductId(productId);

        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(1L);

        User user = new User();
        user.setCustomerProfile(profile);

        Order order = new Order();
        order.setOrderId(orderId);

        ReviewRequestDTO reviewRequest = new ReviewRequestDTO();
        reviewRequest.setOrderId(orderId);
        reviewRequest.setComment("Updated review comment");

        Rating existingRating = new Rating();
        existingRating.setCustomerProfile(profile);
        existingRating.setProduct(product);
        existingRating.setOrder(order);
        existingRating.setComment("Old comment");

        when(productValidator.validateProductExistence(productId)).thenReturn(product);
        when(orderValidator.requireExistingOrder(orderId)).thenReturn(order);
        when(ratingRepository.findByCustomerProfileAndProductAndOrder(profile, product, order)).thenReturn(Optional.of(existingRating));

        // Act
        productService.addOrUpdateReview(productId, user, reviewRequest);

        // Assert
        assertEquals("Updated review comment", existingRating.getComment());
        verify(ratingRepository).save(existingRating);
    }
    @Test
    void testCreateProduct_ImagesUpload_Success() throws IOException {
        // Arrange
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("iPhone");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        request.setImagesUrls(List.of(mockFile));

        Product product = new Product();
        product.setProductId(1L);

        when(productRepository.existsByName("iPhone")).thenReturn(false);
        when(productMapper.mapToProduct(request)).thenReturn(product);

        // Mock cloudinary uploader
        var uploader = mock(com.cloudinary.Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of("secure_url", "http://image.url"));

        when(productRepository.save(product)).thenReturn(product);

        // Act
        productService.createProduct(request);

        // Assert
        verify(productRepository).save(product);
        assertEquals(1, product.getProductImages().size());
        assertEquals("http://image.url", product.getProductImages().get(0).getImageUrl());
    }
    @Test
    void testCreateProduct_WhenIOExceptionThrown_ShouldThrowRuntimeException() throws IOException {
        // Arrange
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("iPhone");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenThrow(new IOException("Simulated IOException"));
        request.setImagesUrls(List.of(mockFile));

        Product product = new Product();
        product.setProductId(1L);

        when(productRepository.existsByName("iPhone")).thenReturn(false);
        when(productMapper.mapToProduct(request)).thenReturn(product);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            productService.createProduct(request);
        });

        assertTrue(thrownException.getMessage().contains("Failed to upload image"));
        assertTrue(thrownException.getCause() instanceof IOException);
    }
    @Test
    void testFindProductByCategory_ThrowsRuntimeExceptionOnUnexpectedError() {
        // Arrange
        String category = "LAPTOPS";
        Pageable pageable = PageRequest.of(0, 10);

        // Simulate an unexpected exception thrown by repository method
        when(productRepository.findByCategory(Category.valueOf(category), pageable))
                .thenThrow(new NullPointerException("Simulated NPE"));

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            productService.findProductByCategory(pageable, category);
        });

        assertEquals("Failed to fetch products. Please try again later.", thrownException.getMessage());
    }



}