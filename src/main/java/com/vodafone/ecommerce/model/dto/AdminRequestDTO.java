package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.model.enums.AccountStatus;
import com.vodafone.ecommerce.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRequestDTO {

    @NotBlank(message = "Email is required")
    @Email
    @Pattern(
            regexp = ValidationPatterns.EMAIL_REGEX,
            message = "Invalid email format"
    )
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = ValidationPatterns.PASSWORD_REGEX,
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    private String password;

    private AccountStatus accountStatus;
}