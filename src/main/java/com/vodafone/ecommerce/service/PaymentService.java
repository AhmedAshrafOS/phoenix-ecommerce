package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.CheckoutRequestDTO;
import com.vodafone.ecommerce.model.dto.CheckoutSessionResponseDTO;

public interface PaymentService {

    CheckoutSessionResponseDTO process(CheckoutRequestDTO request);

    String getGateway();
}
