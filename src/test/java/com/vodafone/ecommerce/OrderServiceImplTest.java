package com.vodafone.ecommerce;

import com.vodafone.ecommerce.designpattern.factory.PaymentProcessorFactory;
import com.vodafone.ecommerce.exception.ForbiddenActionException;
import com.vodafone.ecommerce.mapper.OrderItemMapper;
import com.vodafone.ecommerce.mapper.OrderMapper;
import com.vodafone.ecommerce.mapper.OrderResponseMapper;
import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.*;
import com.vodafone.ecommerce.model.enums.OrderStatus;
import com.vodafone.ecommerce.model.enums.PaymentMethod;
import com.vodafone.ecommerce.repository.OrderRepository;
import com.vodafone.ecommerce.service.CartItemService;
import com.vodafone.ecommerce.service.PaymentService;
import com.vodafone.ecommerce.service.impl.OrderServiceImpl;
import com.vodafone.ecommerce.validation.CartStockValidator;
import com.vodafone.ecommerce.validation.CustomerProfileValidator;
import com.vodafone.ecommerce.validation.OrderValidator;
import com.vodafone.ecommerce.validation.ShoppingCartValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShoppingCartValidator shoppingCartValidator;
    @Mock
    private CustomerProfileValidator customerProfileValidator;
    @Mock
    private OrderValidator orderValidator;
    @Mock
    private CartStockValidator cartStockValidator;
    @Mock
    private PaymentProcessorFactory processorFactory;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderResponseMapper orderResponseMapper;
    @Mock
    private CartItemService cartItemService;
    @Mock
    private PaymentService paymentService;

    private User user;
    private ShoppingCart cart;
    private CartItem cartItem;
    private Order order;
    private AutoCloseable closeable;
    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Setup CustomerProfile
        CustomerProfile profile = new CustomerProfile();
        profile.setCustomerProfileId(1L);

        // Setup User
        user = new User();
        user.setUserId(10L);
        user.setCustomerProfile(profile);

        Product product;
        // Setup Product
        product = new Product();
        product.setProductId(100L);
        product.setName("Test Product");
        product.setPrice(120.0);               // Original price
        product.setDiscountPercentage(20.0);   // 20% discount → final price = 96.0
        product.setStockQuantity(10);

        // Setup CartItem
        cartItem = new CartItem();
        cartItem.setCartId(cart);  // Will be overwritten later after cart is initialized
        cartItem.setProductId(product);
        cartItem.setQuantity(2);

        // Setup ShoppingCart
        cart = new ShoppingCart();
        cart.setCartId(1L);
        cart.setCustomerProfile(profile);
        cart.setCartItems(List.of(cartItem));

        // Now that cart is initialized, set it on cartItem
        cartItem.setCartId(cart);

        // Setup Order
        order = new Order();
        order.setOrderId(99L);
        order.setTotalAmount(product.getFinalUnitPrice() * cartItem.getQuantity());
        order.setCustomerProfile(profile);
    }
    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }


    @Test
    void testCreateOrder_WithCOD_ShouldReturnSession() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setPaymentMethod(PaymentMethod.COD);

        when(shoppingCartValidator.requireCart(1L)).thenReturn(cart);
        doNothing().when(cartStockValidator).validateAndClean(cart);
        when(orderMapper.toOrder(eq(dto), eq(cart), anyDouble())).thenReturn(order);
        when(orderRepository.save(any())).thenReturn(order);

        CheckoutSessionResponseDTO result = orderService.createOrder(dto, user);

        assertNotNull(result);
        assertEquals("99", result.getCheckoutUrl());
    }

    @Test
    void testCreateOrder_WithOnlinePayment_ShouldCallProcessor() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setPaymentMethod(PaymentMethod.STRIPE);
        dto.setCurrency("USD");
        dto.setEmail("test@example.com");

        when(shoppingCartValidator.requireCart(1L)).thenReturn(cart);
        doNothing().when(cartStockValidator).validateAndClean(cart);
        when(orderMapper.toOrder(eq(dto), eq(cart), anyDouble())).thenReturn(order);
        when(orderRepository.save(any())).thenReturn(order);
        when(processorFactory.getProcessor("STRIPE")).thenReturn(paymentService);
        when(paymentService.process(any())).thenReturn(new CheckoutSessionResponseDTO("session123"));

        CheckoutSessionResponseDTO result = orderService.createOrder(dto, user);

        assertEquals("session123", result.getCheckoutUrl());
    }

    @Test
    void testConfirmOrder_Success() {
        order.setStatus(OrderStatus.PENDING);
        when(orderValidator.requireExistingOrder(99L)).thenReturn(order);
        when(shoppingCartValidator.requireCart(1L)).thenReturn(cart);

        orderService.confirmOrder(99L, user);

        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
        verify(orderRepository).save(order);
        verify(cartItemService).clearCart(1L);
    }

    @Test
    void testConfirmOrder_Forbidden() {
        CustomerProfile otherProfile = new CustomerProfile();
        otherProfile.setCustomerProfileId(2L);
        order.setCustomerProfile(otherProfile);
        when(orderValidator.requireExistingOrder(99L)).thenReturn(order);

        assertThrows(ForbiddenActionException.class, () -> orderService.confirmOrder(99L, user));
    }

    @Test
    void testDeleteOrderById_Success() {
        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        when(orderValidator.requireExistingOrder(99L)).thenReturn(order);

        orderService.deleteOrderById(99L);

        verify(orderRepository).deleteById(99L);
    }

    @Test
    void testDeleteOrderById_Delivered_ShouldThrow() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderValidator.requireExistingOrder(99L)).thenReturn(order);

        assertThrows(IllegalStateException.class, () -> orderService.deleteOrderById(99L));
    }

    @Test
    void testGetTotalAmount_ShouldCalculateCorrectly() {
        double total = orderService.getTotalAmount(List.of(cartItem));
        assertEquals(192.0, total);
    }

    @Test
    void testFindOrdersByCustomerId_ShouldReturnList() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(order));
        when(orderResponseMapper.toOrderDTOList(any())).thenReturn(List.of(new OrderResponseDTO()));

        List<OrderResponseDTO> result = orderService.findOrdersByCustomerId(user);

        assertEquals(1, result.size());
    }

    @Test
    void testFindAllOrders_ShouldReturnPage() {
        Page<Order> page = new PageImpl<>(List.of(order));

        // Fix: specify Pageable.class to avoid ambiguity
        when(orderRepository.findAll(Mockito.any(Pageable.class))).thenReturn(page);
        when(orderResponseMapper.toOrderDTO(Mockito.any())).thenReturn(new OrderResponseDTO());

        Page<OrderResponseDTO> result = orderService.findAllOrders(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindOrderById_ShouldReturnDto() {
        when(orderValidator.requireExistingOrder(99L)).thenReturn(order);
        when(orderResponseMapper.toOrderDTO(order)).thenReturn(new OrderResponseDTO());

        OrderResponseDTO result = orderService.findOrderById(99L);

        assertNotNull(result);
    }

    @Test
    void testGetFinalUnitPrice_NoDiscount() {
        Product product = new Product();
        product.setPrice(100.0);
        product.setDiscountPercentage(0.0);

        assertEquals(100.0, product.getFinalUnitPrice());
    }

    @Test
    void testGetFinalUnitPrice_WithDiscount() {
        Product product = new Product();
        product.setPrice(200.0);
        product.setDiscountPercentage(10.0); // 10%

        assertEquals(180.0, product.getFinalUnitPrice());
    }
    @Test
    void testUpdateOrderStatus_ShouldUpdateSuccessfully() {
        Long orderId = 99L;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(orderValidator.requireExistingOrder(orderId)).thenReturn(order);

        orderService.updateOrderStatus(orderId, OrderStatus.READY_FOR_DELIVERY);

        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
        verify(orderRepository).save(order);
    }
    @Test
    void testGetTotalAmount_AllBranches() {
        Product validProduct = new Product();
        validProduct.setPrice(100.0);
        validProduct.setDiscountPercentage(20.0); // final = 80.0

        Product nullPriceProduct = new Product(); // no price or discount set = null final

        CartItem validItem = new CartItem();
        validItem.setProductId(validProduct);
        validItem.setQuantity(1);

        CartItem nullItem = null;

        CartItem noProductItem = new CartItem();
        noProductItem.setProductId(null);

        CartItem nullPriceItem = new CartItem();
        nullPriceItem.setProductId(nullPriceProduct);
        nullPriceItem.setQuantity(2); // price is null

        List<CartItem> items = new ArrayList<>();
        items.add(validItem);
        items.add(nullItem);           // allowed in ArrayList
        items.add(noProductItem);
        items.add(nullPriceItem);

        double total = orderService.getTotalAmount(items);

        assertEquals(80.0, total); // Only validItem contributes
    }
    @Test
    void testCreateOrder_MapOrderItems_AllBranches() {
        // Valid product
        Product validProduct = new Product();
        validProduct.setPrice(100.0);
        validProduct.setDiscountPercentage(20.0); // Final = 80.0

        // Product without price
        Product nullPriceProduct = new Product(); // getFinalUnitPrice() returns null unless price is set

        // Valid cart item
        CartItem validItem = new CartItem();
        validItem.setProductId(validProduct);
        validItem.setQuantity(2);

        CartItem nullItem = null;
        CartItem noProductItem = new CartItem();
        noProductItem.setProductId(null);

        CartItem nullPriceItem = new CartItem();
        nullPriceItem.setProductId(nullPriceProduct);
        nullPriceItem.setQuantity(1);

        List<CartItem> cartItems = new ArrayList<>(Arrays.asList(
                validItem, nullItem, noProductItem, nullPriceItem
        ));
        cart.setCartItems(cartItems);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentMethod(PaymentMethod.COD);

        when(shoppingCartValidator.requireCart(1L)).thenReturn(cart);
        doNothing().when(cartStockValidator).validateAndClean(cart);
        when(orderMapper.toOrder(eq(request), eq(cart), anyDouble())).thenReturn(order);
        when(orderRepository.save(any())).thenReturn(order);

        OrderItem mockOrderItem = new OrderItem();
        when(orderItemMapper.toOrderItem(eq(validItem), anyDouble(), eq(order), anyDouble()))
                .thenReturn(mockOrderItem);

        CheckoutSessionResponseDTO result = orderService.createOrder(request, user);

        assertNotNull(result);
        verify(orderItemMapper, times(1)).toOrderItem(eq(validItem), anyDouble(), eq(order), anyDouble());
    }
    @Test
    void testConfirmOrder_OrderDoesNotBelongToUser_ShouldThrow() {
        CustomerProfile customer = new CustomerProfile();
        customer.setCustomerProfileId(99L);
        order.setCustomerProfile(customer);
// Different ID

        when(orderValidator.requireExistingOrder(1L)).thenReturn(order);

        ForbiddenActionException ex = assertThrows(ForbiddenActionException.class,
                () -> orderService.confirmOrder(1L, user));
        assertEquals("This order does not belong to you", ex.getMessage());
    }
    @Test
    void testConfirmOrder_StatusNotPending_ShouldDoNothing() {
        order.setStatus(OrderStatus.DELIVERED); // Not PENDING

        when(orderValidator.requireExistingOrder(1L)).thenReturn(order);
        order.setCustomerProfile(user.getCustomerProfile());

        orderService.confirmOrder(1L, user);

        verify(orderRepository, never()).save(any());
        verify(cartItemService, never()).clearCart(any());
    }
    @Test
    void testConfirmOrder_ValidPendingOrder_ShouldUpdateStatusAndClearCart() {
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerProfile(user.getCustomerProfile());

        when(orderValidator.requireExistingOrder(1L)).thenReturn(order);
        when(shoppingCartValidator.requireCart(1L)).thenReturn(cart);

        orderService.confirmOrder(1L, user);

        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
        verify(orderRepository).save(order);
        verify(cartItemService).clearCart(cart.getCartId());
    }





}
