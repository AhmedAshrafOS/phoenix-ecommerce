package com.vodafone.ecommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vodafone.ecommerce.mapper.ProductMapper;
import com.vodafone.ecommerce.model.dto.ProductResponseDTO;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.repository.ProductRepository;
import com.vodafone.ecommerce.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;

class SearchServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchByKeyword_ReturnsMappedPage() {
        // Arrange
        String keyword = "phone";
        Pageable pageable = PageRequest.of(0, 2);

        Product product1 = new Product();
        product1.setProductId(1L);
        Product product2 = new Product();
        product2.setProductId(2L);

        Page<Product> productPage = new PageImpl<>(List.of(product1, product2), pageable, 5);

        ProductResponseDTO dto1 = new ProductResponseDTO();
        ProductResponseDTO dto2 = new ProductResponseDTO();

        when(productRepository.searchKey(keyword.toLowerCase(), pageable)).thenReturn(productPage);
        when(productMapper.mapToProductResponseDTO(product1)).thenReturn(dto1);
        when(productMapper.mapToProductResponseDTO(product2)).thenReturn(dto2);

        // Act
        Page<ProductResponseDTO> result = searchService.searchByKeyword(keyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertTrue(result.getContent().containsAll(List.of(dto1, dto2)));

        verify(productRepository).searchKey(keyword.toLowerCase(), pageable);
        verify(productMapper).mapToProductResponseDTO(product1);
        verify(productMapper).mapToProductResponseDTO(product2);
    }
}

