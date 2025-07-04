package com.vodafone.ecommerce.exception;

public class LowStockException extends RuntimeException {
    public LowStockException(String message) {
        super(message);
    }
}
