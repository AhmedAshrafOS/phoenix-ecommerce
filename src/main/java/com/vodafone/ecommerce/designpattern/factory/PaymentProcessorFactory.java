package com.vodafone.ecommerce.designpattern.factory;

import com.vodafone.ecommerce.exception.UnsupportedGatewayException;
import com.vodafone.ecommerce.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentProcessorFactory {

    private final List<PaymentService> processors;

    @Autowired
    public PaymentProcessorFactory(List<PaymentService> processors) {
        this.processors = processors;
    }

    public PaymentService getProcessor(String gatewayName) {
        return processors.stream()
                .filter(p -> p.getGateway().equalsIgnoreCase(gatewayName))
                .findFirst()
                .orElseThrow(() -> new UnsupportedGatewayException("Unsupported gateway: " + gatewayName));
    }
}
