package com.vodafone.ecommerce.validation;

import com.vodafone.ecommerce.exception.NotFoundException;
import com.vodafone.ecommerce.model.entity.Order;
import com.vodafone.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for order-related existence and business rules.
 */
@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final OrderRepository orderRepository;

    /**
     * Ensures the order exists for the given ID.
     *
     * @param orderId the ID of the order to validate
     * @return the found Order entity
     * @throws NotFoundException if the order does not exist
     */
    public Order requireExistingOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found for id: " + orderId));
    }
}

