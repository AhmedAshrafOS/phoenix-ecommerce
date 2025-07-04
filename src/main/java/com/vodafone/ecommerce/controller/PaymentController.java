package com.vodafone.ecommerce.controller;

import com.vodafone.ecommerce.designpattern.factory.PaymentProcessorFactory;
import com.vodafone.ecommerce.model.dto.CheckoutRequestDTO;
import com.vodafone.ecommerce.model.dto.CheckoutSessionResponseDTO;
import com.vodafone.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessorFactory processorFactory;

    @PostMapping(path = "/checkout", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CheckoutSessionResponseDTO> checkout(@Valid @RequestBody CheckoutRequestDTO request) {
        PaymentService processor = processorFactory.getProcessor(request.getPaymentMethod());
        CheckoutSessionResponseDTO response = processor.process(request);
        return ResponseEntity.ok(response);
    }
}
