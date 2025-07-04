package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.ProductImage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends BaseRepository<ProductImage, Long> {

    List<ProductImage> findProductImageByProductProductId(Long productProductId);
}
