package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerUpdateRequestDTO implements UserUpdateRequestDTO {

    private String username;

    private String firstName;

    private String lastName;

    @Pattern(
            regexp = ValidationPatterns.PHONE_NUMBER_REGEX,
            message = "Invalid Egyptian phone number"
    )
    private String phone;

    @Email(message = "Invalid email format")
    @Pattern(
            regexp = ValidationPatterns.EMAIL_REGEX,
            message = "Invalid email format"
    )
    private String email;

    private String oldPassword;

    @Pattern(
            regexp = ValidationPatterns.PASSWORD_REGEX,
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    private String newPassword;

    private String confirmPassword;

    private AddressResponseDTO address;
}
