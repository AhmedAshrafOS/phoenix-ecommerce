package com.vodafone.ecommerce.exception;

public class UnsupportedGatewayException extends RuntimeException {
    public UnsupportedGatewayException(String gatewayName) {
        super("Unsupported gateway: " + gatewayName);
    }
}
