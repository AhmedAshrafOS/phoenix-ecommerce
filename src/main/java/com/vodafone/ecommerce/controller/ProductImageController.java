package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.ProductImageRequestDTO;
import com.vodafone.ecommerce.model.dto.ProductImageResponseDTO;
import com.vodafone.ecommerce.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-images")
public class ProductImageController {

    public final ProductImageService productImageService;

    @PostMapping(
            path = "/{productId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ProductImageResponseDTO createProductImage(
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageRequestDTO request) {
        return productImageService.insertNewImage(productId, request);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @GetMapping(path = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductImageResponseDTO>> findProductImage(@PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.OK).body(productImageService.findImagesByProductId(productId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @DeleteMapping(path = "/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProductImage(@PathVariable Long productId, @RequestParam int displayOrder) {
        productImageService.deleteImageByProductId(productId, displayOrder);
    }
}
