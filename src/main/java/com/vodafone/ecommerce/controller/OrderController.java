package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.model.dto.CheckoutSessionResponseDTO;
import com.vodafone.ecommerce.model.dto.OrderRequestDTO;
import com.vodafone.ecommerce.model.dto.OrderResponseDTO;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.model.enums.OrderStatus;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CheckoutSessionResponseDTO> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @Valid @RequestBody OrderRequestDTO createOrderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(createOrderRequestDTO, userDetails.getUser()));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping(path = "/customer", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderResponseDTO>> findOrdersByCustomerId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(orderService.findOrdersByCustomerId(userDetails.getUser()));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<OrderResponseDTO> findAllOrders(Pageable pageable) {
        return orderService.findAllOrders(pageable);
    }

    @GetMapping(path = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponseDTO> findOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.findOrderById(orderId));
    }

    @PatchMapping(path = "/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public void updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus orderStatus) {
        orderService.updateOrderStatus(orderId, orderStatus);
    }

    @DeleteMapping(path = "/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderById(@PathVariable Long orderId) {
        orderService.deleteOrderById(orderId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{orderId}/confirm")
    @ResponseStatus(HttpStatus.OK)
    public void confirmOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        orderService.confirmOrder(orderId, user);
    }
}
