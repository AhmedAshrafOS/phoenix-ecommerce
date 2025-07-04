package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.CheckoutSessionResponseDTO;
import com.vodafone.ecommerce.model.dto.OrderRequestDTO;
import com.vodafone.ecommerce.model.dto.OrderResponseDTO;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    CheckoutSessionResponseDTO createOrder(OrderRequestDTO orderRequestDTO, User user);

    List<OrderResponseDTO> findOrdersByCustomerId(User user);

    Page<OrderResponseDTO> findAllOrders(Pageable pageable);

    OrderResponseDTO findOrderById(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus orderStatus);

    void deleteOrderById(Long orderId);

    void confirmOrder(Long orderId, User user);
}
