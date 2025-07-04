package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.entity.CartItem;
import com.vodafone.ecommerce.model.entity.Order;
import com.vodafone.ecommerce.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    /**
     * Maps a CartItem into an OrderItem, using provided price and order.
     *
     * @param cartItem  the cart item
     * @param itemPrice the discounted price
     * @param order     the order the item belongs to
     * @return the mapped OrderItem
     */
    @Mapping(target = "product", source = "cartItem.productId")
    @Mapping(target = "quantity", source = "cartItem.quantity")
    @Mapping(target = "unitPrice", source = "itemPrice")
    @Mapping(target = "discountAmount", source = "discountedPrice")
    @Mapping(target = "order", source = "order")
    @Mapping(target = "createdDate", source = "cartItem.createdDate")
    @Mapping(target = "updatedDate", source = "cartItem.updatedDate")
    OrderItem toOrderItem(CartItem cartItem, Double itemPrice, Order order, Double discountedPrice);
}
