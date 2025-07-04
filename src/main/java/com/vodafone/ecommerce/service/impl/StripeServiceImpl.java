package com.vodafone.ecommerce.service.impl;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.vodafone.ecommerce.exception.StripeCheckoutException;
import com.vodafone.ecommerce.model.dto.CheckoutRequestDTO;
import com.vodafone.ecommerce.model.dto.CheckoutSessionResponseDTO;
import com.vodafone.ecommerce.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class StripeServiceImpl implements PaymentService {

    @Override
    public CheckoutSessionResponseDTO process(CheckoutRequestDTO request) {
        try {
            double amount = request.getAmount();
            long finalAmount = Math.round(amount * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("https://ecommerce-phoenix.netlify.app/orders/success/"+request.getOrderId())
                    .setCancelUrl("https://ecommerce-phoenix.netlify.app/orders/failed/"+request.getOrderId())
                    .setCustomerEmail(request.getEmail())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency())
                                                    .setUnitAmount(finalAmount)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order Payment")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return new CheckoutSessionResponseDTO(session.getUrl());
        } catch (Exception e) {
            throw new StripeCheckoutException("Failed to create Stripe Checkout Session", e);
        }
    }

    @Override
    public String getGateway() {
        return "STRIPE";
    }
}
