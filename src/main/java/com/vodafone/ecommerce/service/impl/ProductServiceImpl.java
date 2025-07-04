package com.vodafone.ecommerce.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vodafone.ecommerce.exception.ForbiddenActionException;
import com.vodafone.ecommerce.mapper.ProductMapper;
import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.*;
import com.vodafone.ecommerce.model.enums.Category;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.repository.RatingRepository;
import com.vodafone.ecommerce.service.ProductService;
import com.vodafone.ecommerce.validation.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final Cloudinary cloudinary;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final RatingRepository ratingRepository;
    private final com.vodafone.ecommerce.validation.ProductValidator productValidator;
    private final OrderValidator orderValidator;

    public void createProduct(ProductRequestDTO request) {

        log.info("Creating new product");

        if (productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("A product with the same name exists");
        }

        Product product = productMapper.mapToProduct(request);
        List<ProductImage> productImages = getImagesUrlsFromCloudinary(request.getImagesUrls(), product);
        product.setProductImages(productImages);

        Product saved = productRepository.save(product);

        log.info("Product created with ID: {}", saved.getProductId());
    }


    public ProductResponseDetailsDTO findProductById(Long id) {
        log.debug("Retrieving product by ID: {}", id);
        Product product = productValidator.validateProductExistence(id);
        ProductResponseDetailsDTO productResponseDetailsDTO = productMapper.mapToProductResponseDetailsDTO(product);
        productResponseDetailsDTO.setTotalFinalPrice(product.getFinalUnitPrice());
        return productResponseDetailsDTO;
    }

    public Page<ProductResponseDTO> findAllProducts(Pageable pageable) {
        log.info("Fetching paginated product list: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponseDTO> productResponseDTOs = productPage.stream().map(productMapper::mapToProductResponseDTO).toList();

        log.debug("Returning {} products", productResponseDTOs.size());
        return new PageImpl<>(productResponseDTOs, pageable, productPage.getTotalElements());
    }

    public void updateProductById(Long id, ProductRequestDTO request) {
        log.info("Updating product with ID: {}", id);

        Product product = productValidator.validateProductExistence(id);

        product.setName(request.getName());
        product.setFeatures(request.getFeatures());
        product.setSpecs(request.getSpecs());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setBrandName(request.getBrandName());
        product.setCategory(request.getCategory());
        product.setDiscountPercentage(request.getDiscountPercentage());

        productRepository.save(product);

        log.info("Product with ID {} updated successfully", id);
    }

    public void rateProduct(Long productId, User user, RatingRequestDTO request) {
        log.info("Processing rating: productId={}, orderId={}, user={}", productId, request.getOrderId(), user.getUsername());

        Product product = productValidator.validateProductExistence(productId);
        CustomerProfile profile = user.getCustomerProfile();

        Order order = orderValidator.requireExistingOrder(request.getOrderId());

        if (!Objects.equals(order.getCustomerProfile().getCustomerProfileId(), profile.getCustomerProfileId())) {
            log.warn("User {} attempted to rate product {} in order {} that does not belong to them", user.getUsername(), productId, request.getOrderId());
            throw new ForbiddenActionException("You cannot rate a product from an order that does not belong to you.");
        }

        Rating existingRating = ratingRepository.findByCustomerProfileAndProductAndOrder(profile, product, order).orElse(null);

        int newRatingValue = request.getRatingValue();

        if (existingRating == null) {
            createNewRating(profile, product, order, newRatingValue, request.getComment());
            updateStatsForNewRating(product, newRatingValue);
            log.info("New rating submitted for product={}, order={}", productId, request.getOrderId());
            return;
        }

        int oldRatingValue = existingRating.getRatingValue();
        if (oldRatingValue == newRatingValue) {
            log.debug("No change in rating for product={}, order={}", productId, request.getOrderId());
            return;
        }

        existingRating.setRatingValue(newRatingValue);
        ratingRepository.save(existingRating);
        updateStatsForChangedRating(product, oldRatingValue, newRatingValue);
        log.info("Updated rating for product={}, order={} (old={}, new={})", productId, request.getOrderId(), oldRatingValue, newRatingValue);
    }

    private void createNewRating(CustomerProfile profile, Product product, Order order, int ratingValue, String comment) {
        Rating rating = new Rating();
        rating.setCustomerProfile(profile);
        rating.setProduct(product);
        rating.setOrder(order);
        rating.setRatingValue(ratingValue);
        rating.setComment(comment);
        ratingRepository.save(rating);
    }

    public List<RatingResponseDTO> getRatingResponsesForProduct(Long productId) {
        return ratingRepository.findByProduct_ProductId(productId)
                .stream()
                .map(rating -> {
                    String firstName = rating.getCustomerProfile().getFirstName();
                    String lastName = rating.getCustomerProfile().getLastName();
                    return new RatingResponseDTO(rating.getComment(), firstName, lastName);
                })
                .toList();
    }

    public void addOrUpdateReview(Long productId, User user, ReviewRequestDTO reviewRequest) {
        log.info("User is adding/updating a review for product ID: {}", productId);

        Product product = productValidator.validateProductExistence(productId);

        Order order = orderValidator.requireExistingOrder(reviewRequest.getOrderId());

        CustomerProfile profile = user.getCustomerProfile();

        Rating rating = ratingRepository.findByCustomerProfileAndProductAndOrder(profile, product, order).orElseThrow(() -> {
            log.warn("Attempted to review product {} without rating in order {}", productId, reviewRequest.getOrderId());
            return new RuntimeException("You must rate the product before reviewing.");
        });

        rating.setComment(reviewRequest.getComment());
        ratingRepository.save(rating);

        log.info("Review added/updated for product ID: {}, order ID: {}", productId, reviewRequest.getOrderId());
    }

    private void updateStatsForNewRating(Product product, int newRating) {
        int count = product.getReviewCount() == null ? 0 : product.getReviewCount();
        double avg = product.getAverageRating() == null ? 0.0 : product.getAverageRating();

        int updatedCount = count + 1;
        double updatedAvg = ((avg * count) + newRating) / updatedCount;

        product.setReviewCount(updatedCount);
        product.setAverageRating(updatedAvg);
        productRepository.save(product);

        log.debug("New stats for product ID {} → average: {}, count: {}", product.getProductId(), updatedAvg, updatedCount);
    }

    private void updateStatsForChangedRating(Product product, int oldRating, int newRating) {
        int count = product.getReviewCount();
        double avg = product.getAverageRating();

        double updatedAvg = ((avg * count) - oldRating + newRating) / count;

        product.setAverageRating(updatedAvg);
        productRepository.save(product);

        log.debug("Updated stats for product ID {} → new average after rating change: {}", product.getProductId(), updatedAvg);
    }

    public void deleteProductById(Long id) {
        log.warn("Deleting product with ID: {}", id);
        Product product = productValidator.validateProductExistence(id);
        productRepository.delete(product);
        log.info("Product with ID {} deleted", id);
    }

    private List<ProductImage> getImagesUrlsFromCloudinary(List<MultipartFile> images, Product product) {
        List<ProductImage> productImages = new ArrayList<>();

        int order = 0;
        for (MultipartFile file : images) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> resp = cloudinary.uploader()
                        .upload(file.getBytes(),
                                ObjectUtils.asMap("folder", "ecommerce", "public_id",
                                        "prod_" + product.getProductId() + "_" + order)
                        );
                String url = (String) resp.get("secure_url");

                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                productImage.setImageUrl(url);
                productImage.setDisplayOrder(order++);
                productImages.add(productImage);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        return productImages;
    }

    public Page<ProductResponseDTO> findProductByCategory(Pageable pageable, String category) {
        log.info("Searching for products in category: {}, page number: {}, page size: {}, sort: {}",
                category, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<Product> productsPage = productRepository.findByCategory(Category.valueOf(category), pageable);

            log.debug("Fetched {} products from database for category {}", productsPage.getNumberOfElements(), category);

            Page<ProductResponseDTO> responsePage = productsPage.map(productMapper::mapToProductResponseDTO);

            log.info("Mapped {} products to ProductResponseDTO", responsePage.getNumberOfElements());

            return responsePage;
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument while searching by category: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid pagination or category input.");
        } catch (Exception ex) {
            log.error("Unexpected error occurred while searching products by category", ex);
            throw new RuntimeException("Failed to fetch products. Please try again later.");
        }
    }
}
