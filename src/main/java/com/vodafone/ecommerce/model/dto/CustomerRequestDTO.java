package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequestDTO {

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(
            regexp = ValidationPatterns.EMAIL_REGEX,
            message = "Invalid email format"
    )
    public String email;

    @NotNull(message = "Username is required")
    public String username;

    @NotNull(message = "Password is required")
    @Pattern(
            regexp = ValidationPatterns.PASSWORD_REGEX,
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    public String password;

    @NotNull(message = "First Name is required")
    public String firstName;

    @NotNull(message = "Last Name is required")
    public String lastName;

    @NotNull(message = "Phone Number is required")
    @Pattern(
            regexp = ValidationPatterns.PHONE_NUMBER_REGEX,
            message = "Invalid Egyptian phone number"
    )
    public String phoneNumber;
}
