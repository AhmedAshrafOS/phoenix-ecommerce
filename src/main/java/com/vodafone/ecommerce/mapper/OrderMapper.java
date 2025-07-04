package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.AddressResponseDTO;
import com.vodafone.ecommerce.model.dto.OrderRequestDTO;
import com.vodafone.ecommerce.model.entity.Order;
import com.vodafone.ecommerce.model.entity.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface OrderMapper {

    @Mapping(target = "orderNumber", expression = "java(\"PHOENIX-\" + LocalDateTime.now())")
    @Mapping(target = "totalAmount", source = "totalPrice")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "shippingAddress", expression = "java(formatAddress(orderRequestDTO.getAddress()))")
    @Mapping(target = "paymentMethod", source = "orderRequestDTO.paymentMethod")
    @Mapping(target = "customerProfile", source = "cart.customerProfile")
    Order toOrder(OrderRequestDTO orderRequestDTO, ShoppingCart cart, double totalPrice);

    default String formatAddress(AddressResponseDTO address) {
        if (address == null) return "";

        StringBuilder sb = new StringBuilder();

        if (address.getStreet() != null) sb.append(address.getStreet()).append(", ");
        if (address.getFloor() != null) sb.append("Floor ").append(address.getFloor()).append(", ");
        if (address.getApartmentNumber() != null)
            sb.append("Apartment ").append(address.getApartmentNumber()).append(", ");
        if (address.getBuildingNumber() != null)
            sb.append("Building ").append(address.getBuildingNumber()).append(", ");
        if (address.getCity() != null) sb.append(address.getCity()).append(", ");
        if (address.getGovernorate() != null) sb.append(address.getGovernorate()).append(", ");
        if (address.getCountry() != null) sb.append(address.getCountry());
        return sb.toString().replaceAll(", $", "");
    }
}
