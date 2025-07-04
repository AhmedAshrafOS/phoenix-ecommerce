package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public void createProduct(@Valid @ModelAttribute ProductRequestDTO request) {
        productService.createProduct(request);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDetailsDTO> findProductById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findProductById(id));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ProductResponseDTO>> findAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.findAllProducts(pageable));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public void updateProductById(@PathVariable Long id, @RequestBody ProductRequestDTO request) {
        productService.updateProductById(id, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public void deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping(path = "/{id}/rate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void rateProduct(@PathVariable("id") Long productId, @Valid @RequestBody RatingRequestDTO ratingRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        productService.rateProduct(productId, user, ratingRequest);
    }

    @GetMapping(path = "/{id}/review", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RatingResponseDTO>> findCommentsProduct(@PathVariable("id") Long productId) {
       return ResponseEntity.ok(productService.getRatingResponsesForProduct(productId));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping(path = "/{id}/review", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void reviewProduct(@PathVariable("id") Long productId, @Valid @RequestBody ReviewRequestDTO request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        productService.addOrUpdateReview(productId, user, request);
    }

    @GetMapping(path = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ProductResponseDTO>> findProductByCategory(Pageable pageable, @RequestParam String category) {
        return ResponseEntity.ok(productService.findProductByCategory(pageable, category));
    }
}
