package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.OrderResponseDTO;
import com.vodafone.ecommerce.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = OrderItemResponseMapper.class)
public interface OrderResponseMapper {

    @Mapping(target = "orderId", source = "order.orderId")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "totalAmount", source = "order.totalAmount")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "paymentMethod", source = "order.paymentMethod")
    @Mapping(target = "shippingAddress", source = "order.shippingAddress")
    @Mapping(target = "createdDate", source = "order.createdDate")
    @Mapping(target = "orderItems", source = "order.orderItems")
    @Mapping(target = "firstName", source = "order.customerProfile.firstName")
    @Mapping(target = "lastName", source = "order.customerProfile.lastName")
    @Mapping(target = "phone", source = "order.customerProfile.phoneNumber")
    OrderResponseDTO toOrderDTO(Order order);

    List<OrderResponseDTO> toOrderDTOList(List<Order> orders);
}
