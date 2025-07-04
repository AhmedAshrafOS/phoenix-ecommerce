package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.OrderItemResponseDTO;
import com.vodafone.ecommerce.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemResponseMapper {

    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", expression = "java(orderItem.getUnitPrice().doubleValue())")
    @Mapping(target = "discountAmount", expression = "java(orderItem.getDiscountAmount() != null ? orderItem.getDiscountAmount().doubleValue() : 0.0)")
    OrderItemResponseDTO mapToDTO(OrderItem orderItem);
}
