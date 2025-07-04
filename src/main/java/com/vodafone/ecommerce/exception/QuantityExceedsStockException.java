package com.vodafone.ecommerce.exception;

public class QuantityExceedsStockException extends RuntimeException {
    public QuantityExceedsStockException(String message) {
        super(message);
    }
}
