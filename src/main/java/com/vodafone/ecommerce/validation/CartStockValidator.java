package com.vodafone.ecommerce.validation;

import com.vodafone.ecommerce.exception.LowStockException;
import com.vodafone.ecommerce.model.entity.CartItem;
import com.vodafone.ecommerce.model.entity.Product;
import com.vodafone.ecommerce.model.entity.ShoppingCart;
import com.vodafone.ecommerce.repository.CartItemRepository;
import com.vodafone.ecommerce.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartStockValidator {

    private final ProductRepository productRepo;
    private final CartItemRepository cartItemRepo;

    /**
     * For each item in {@code cart}, lock the product row in the DB,
     * check stock vs. requested quantity, delete the item and throw if too low.
     */
    @Transactional
    public void validateAndClean(ShoppingCart cart) {
        for (CartItem item : cart.getCartItems()) {
            Long prodId = item.getProductId().getProductId();
            Product product = productRepo
                    .findByIdForUpdate(prodId)
                    .orElseThrow(() -> new LowStockException(
                            "Product not found: id=" + prodId
                    ));

            int inStock = product.getStockQuantity();
            int requested = item.getQuantity();
            if (inStock < requested) {
                cartItemRepo.delete(item);

                throw new LowStockException(
                        "CartItem [cart=" + cart.getCartId()
                                + ", product=" + prodId
                                + "] requested " + requested
                                + " but only " + inStock + " in stock"
                );
            }
            product.setStockQuantity(inStock - requested);
            productRepo.save(product);
        }
    }
}
