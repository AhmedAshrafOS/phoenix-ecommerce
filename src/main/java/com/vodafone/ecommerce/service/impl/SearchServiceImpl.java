package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.mapper.ProductMapper;
import com.vodafone.ecommerce.model.dto.ProductResponseDTO;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductResponseDTO> searchByKeyword(String keyword, Pageable pageable) {

        log.info("Fetching paginated product list: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productPage = productRepository.searchKey(keyword.toLowerCase(), pageable);

        List<ProductResponseDTO> productResponseDTOs = productPage.stream()
                .map(productMapper::mapToProductResponseDTO)
                .toList();

        log.debug("Returning {} products", productResponseDTOs.size());

        return new PageImpl<>(productResponseDTOs, pageable, productPage.getTotalElements());
    }
}
