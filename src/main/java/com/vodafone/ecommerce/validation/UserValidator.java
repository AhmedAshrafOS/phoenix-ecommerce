package com.vodafone.ecommerce.validation;

import com.vodafone.ecommerce.exception.ValidationException;
import com.vodafone.ecommerce.model.dto.UserUpdateRequestDTO;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.repository.CustomerProfileRepository;
import com.vodafone.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void validateEmail(String email) {
        if (email != null && !email.matches(ValidationPatterns.EMAIL_REGEX)) {
            throw new ValidationException("Invalid email format.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException("A user with this email already exists.");
        }
    }

    public void validateUniqueUsername(String username) {
        if (username != null && userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ValidationException("A user with this username already exists.");
        }
    }

    public void validateUniquePhone(String phone) {
        if (phone != null && customerProfileRepository.existsByPhoneNumber(phone)) {
            throw new ValidationException("A user with this phone number already exists.");
        }
    }

    public void validatePasswordChange(UserUpdateRequestDTO dto, String currentPassword) {
        boolean wantsPasswordChange = dto.getOldPassword() != null || dto.getNewPassword() != null || dto.getConfirmPassword() != null;

        if (wantsPasswordChange) {
            if (dto.getOldPassword() == null || dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
                throw new ValidationException("All password fields must be filled to change password.");
            }

            if (!passwordEncoder.matches(dto.getOldPassword(), currentPassword)) {
                throw new ValidationException("Old password is incorrect.");
            }

            if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
                throw new ValidationException("New password and confirm password do not match.");
            }
        }
    }

    public User validateUserExistence(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "The User with Id:[%s] doesn't exist."));
    }
}
