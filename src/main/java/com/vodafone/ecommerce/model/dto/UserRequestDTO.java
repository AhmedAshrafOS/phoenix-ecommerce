package com.vodafone.ecommerce.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserRequestDTO {

    @NotBlank(message = "Username or email must not be null")
    @Schema(defaultValue = "admin")
    private String usernameOrEmail;

    @NotBlank(message = "Password must not be null")
    @Schema(defaultValue = "Admin@123")
    private String password;
}
