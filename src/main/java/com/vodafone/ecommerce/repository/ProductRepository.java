package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.enums.Category;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {

    @Query("SELECT p FROM Product p "
            + "WHERE ( lower(p.name) like %:keyword% ) or ( lower(p.category) like %:keyword% )")
    Page<Product> searchKey(String keyword, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdForUpdate(Long id);

    boolean existsByName(String name);

    /**
     * Search products by category.
     *
     * @param category the category to filter products
     * @param pageable the pagination information
     * @return a page of products belonging to the given category
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category")
    Page<Product> findByCategory(Category category, Pageable pageable);
}
