package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {

    Page<ProductResponseDTO> searchByKeyword(String keyword, Pageable pageable);
}
