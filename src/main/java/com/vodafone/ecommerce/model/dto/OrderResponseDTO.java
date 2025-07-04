package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.model.enums.OrderStatus;
import com.vodafone.ecommerce.model.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OrderResponseDTO {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String orderId;

    private String orderNumber;

    private Double totalAmount;

    private OrderStatus status;

    private PaymentMethod paymentMethod;

    private String shippingAddress;

    private LocalDate createdDate;

    private List<OrderItemResponseDTO> orderItems;
}