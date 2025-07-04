package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.CartItem;
import com.vodafone.ecommerce.model.entity.CartItemPK;
import com.vodafone.ecommerce.model.entity.ShoppingCart;

import java.util.List;

public interface CartItemRepository extends BaseRepository<CartItem, CartItemPK> {

    List<CartItem> findByCartId(ShoppingCart cart);

    void deleteAllByCartId(ShoppingCart cart);
}
