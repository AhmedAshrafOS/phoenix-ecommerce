package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.designpattern.factory.PaymentProcessorFactory;
import com.vodafone.ecommerce.exception.ForbiddenActionException;
import com.vodafone.ecommerce.mapper.OrderItemMapper;
import com.vodafone.ecommerce.mapper.OrderMapper;
import com.vodafone.ecommerce.mapper.OrderResponseMapper;
import com.vodafone.ecommerce.model.dto.CheckoutRequestDTO;
import com.vodafone.ecommerce.model.dto.CheckoutSessionResponseDTO;
import com.vodafone.ecommerce.model.dto.OrderRequestDTO;
import com.vodafone.ecommerce.model.dto.OrderResponseDTO;
import com.vodafone.ecommerce.model.entity.*;
import com.vodafone.ecommerce.model.enums.OrderStatus;
import com.vodafone.ecommerce.model.enums.PaymentMethod;
import com.vodafone.ecommerce.repository.OrderRepository;
import com.vodafone.ecommerce.service.CartItemService;
import com.vodafone.ecommerce.service.OrderService;
import com.vodafone.ecommerce.service.PaymentService;
import com.vodafone.ecommerce.validation.CartStockValidator;
import com.vodafone.ecommerce.validation.CustomerProfileValidator;
import com.vodafone.ecommerce.validation.OrderValidator;
import com.vodafone.ecommerce.validation.ShoppingCartValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ShoppingCartValidator shoppingCartValidator;
    private final CustomerProfileValidator customerProfileValidator;
    private final OrderValidator orderValidator;
    private final CartStockValidator cartStockValidator;
    private final PaymentProcessorFactory processorFactory;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderResponseMapper orderResponseMapper;
    private final CartItemService cartItemService;

    @Transactional
    public CheckoutSessionResponseDTO createOrder(OrderRequestDTO orderRequestDTO, User user) {

        ShoppingCart cart = shoppingCartValidator.requireCart(user.getCustomerProfile().getCustomerProfileId());

        shoppingCartValidator.requireNotEmpty(cart);

        cartStockValidator.validateAndClean(cart);

        double totalPrice = getTotalAmount(cart.getCartItems());

        log.info("Start creating order");
        Order order = orderMapper.toOrder(orderRequestDTO, cart, totalPrice);

        log.info("Start creating order items");
        order.setOrderItems(mapOrderItems(cart.getCartItems(), order));
        if (orderRequestDTO.getPaymentMethod() != PaymentMethod.COD) {
            order.setStatus(OrderStatus.PENDING);

            log.info("Saving order");
            Order savedOrder = orderRepository.save(order);

            log.info("Finish creating order by clearing cart");

            CheckoutRequestDTO checkoutRequestDTO = new CheckoutRequestDTO();
            checkoutRequestDTO.setAmount(savedOrder.getTotalAmount());
            checkoutRequestDTO.setEmail(orderRequestDTO.getEmail());
            checkoutRequestDTO.setCurrency(orderRequestDTO.getCurrency());
            checkoutRequestDTO.setOrderId(savedOrder.getOrderId());
            checkoutRequestDTO.setPaymentMethod(orderRequestDTO.getPaymentMethod().name());
            PaymentService processor = processorFactory.getProcessor(checkoutRequestDTO.getPaymentMethod());


            return processor.process(checkoutRequestDTO);
        }
        order.setStatus(OrderStatus.READY_FOR_DELIVERY);

        log.info("Saving order");
        Order savedOrder = orderRepository.save(order);

        log.info("Finish creating order by clearing cart");
        return new CheckoutSessionResponseDTO(savedOrder.getOrderId().toString());
    }

    public List<OrderResponseDTO> findOrdersByCustomerId(User user) {
        Long customerProfileId = user.getCustomerProfile().getCustomerProfileId();
        customerProfileValidator.requireExistingCustomer(customerProfileId);
        return orderResponseMapper.toOrderDTOList(orderRepository.findByCustomerId(customerProfileId));
    }

    public Page<OrderResponseDTO> findAllOrders(Pageable pageable) {

        Page<Order> orderPage = orderRepository.findAll(pageable);

        List<OrderResponseDTO> orderResponseDTO = orderPage.stream().map(orderResponseMapper::toOrderDTO).toList();

        log.debug("Returning {} Orders", orderResponseDTO.size());
        return new PageImpl<>(orderResponseDTO, pageable, orderPage.getTotalElements());
    }

    public OrderResponseDTO findOrderById(Long orderId) {
        return orderResponseMapper.toOrderDTO(orderValidator.requireExistingOrder(orderId));
    }

    public void updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order tempOrder = orderValidator.requireExistingOrder(orderId);
        tempOrder.setStatus(orderStatus);
        orderRepository.save(tempOrder);
    }

    public void deleteOrderById(Long orderId) {
        Order tempOrder = orderValidator.requireExistingOrder(orderId);
        if (OrderStatus.DELIVERED.equals(tempOrder.getStatus())) {
            throw new IllegalStateException("You can't delete a delivered order");
        }
        orderRepository.deleteById(orderId);
    }

    /**
     * Calculates the total amount of the cart by summing up the price of each item.
     *
     * @param cartItems the list of cart items
     * @return the total amount as a double
     */
    public double getTotalAmount(List<CartItem> cartItems) {
        return cartItems.stream()
                .filter(item -> item != null && item.getProductId() != null && item.getProductId().getFinalUnitPrice() != null)
                .mapToDouble(item -> item.getProductId().getFinalUnitPrice() * item.getQuantity())
                .sum();
    }

    /**
     * Maps a list of {@link CartItem} objects to a list of {@link OrderItem} entities using MapStruct.
     * <p>
     * Each {@code CartItem} is transformed into an {@code OrderItem} with its associated
     * product, quantity, discounted unit price, and linked to the given {@code Order}.
     * Null items or items with missing product or price information are filtered out.
     *
     * @param cartItemList the list of cart items to be converted
     * @param order        the parent order to associate each order item with
     * @return a list of mapped {@code OrderItem} instances
     */
    private List<OrderItem> mapOrderItems(List<CartItem> cartItemList, Order order) {
        return cartItemList.stream()
                .filter(item -> item != null && item.getProductId() != null && item.getProductId().getFinalUnitPrice() != null)
                .map(item -> {
                    Double itemPrice = item.getProductId().getFinalUnitPrice();
                    Double discountedPrice = item.getProductId().getPrice() - itemPrice;
                    return orderItemMapper.toOrderItem(item, itemPrice, order, discountedPrice);
                })
                .toList();
    }

    @Transactional
    public void confirmOrder(Long orderId, User user) {
        Order order = orderValidator.requireExistingOrder(orderId);

        if (!Objects.equals(order.getCustomerProfile().getCustomerProfileId(), user.getCustomerProfile().getCustomerProfileId())) {
            throw new ForbiddenActionException("This order does not belong to you");
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.READY_FOR_DELIVERY);
            orderRepository.save(order);

            ShoppingCart cart = shoppingCartValidator.requireCart(user.getCustomerProfile().getCustomerProfileId());
            cartItemService.clearCart(cart.getCartId());
        }
    }
}
