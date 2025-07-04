package com.vodafone.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDTO {
    private String token;
    private LocalDateTime expiry;
}