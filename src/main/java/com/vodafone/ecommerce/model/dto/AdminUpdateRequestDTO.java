package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateRequestDTO implements UserUpdateRequestDTO {
    @Email
    @Pattern(
            regexp = ValidationPatterns.EMAIL_REGEX,
            message = "Invalid email format"
    )
    private String email;

    private String username;

    private String oldPassword;

    @Pattern(
            regexp = ValidationPatterns.PASSWORD_REGEX,
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    private String newPassword;

    private String confirmPassword;
}