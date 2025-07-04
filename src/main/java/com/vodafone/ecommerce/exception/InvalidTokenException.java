package com.vodafone.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidTokenException extends RuntimeException {
    private final HttpStatus status;

    public InvalidTokenException() {
        super("Invalid token");
        this.status = HttpStatus.UNAUTHORIZED;
    }

    public InvalidTokenException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
    }

    public InvalidTokenException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

}
