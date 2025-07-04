package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.ProductResponseDTO;
import com.vodafone.ecommerce.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    /**
     * Searches products based on the provided keyword and pagination information.
     *
     * @param keyword  the search keyword (must not be empty)
     * @param pageable the pagination information
     * @return a paginated list of products matching the keyword
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ProductResponseDTO>> search(
            @NotBlank(message = "Keyword must not be empty")
            @Size(min = 1, message = "Keyword must be at least 1 character long") @RequestParam String keyword,
            Pageable pageable) {

        Page<ProductResponseDTO> products = searchService.searchByKeyword(keyword, pageable);
        return ResponseEntity.ok(products);
    }
}
