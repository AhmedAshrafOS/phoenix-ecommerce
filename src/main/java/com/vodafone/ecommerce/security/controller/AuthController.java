package com.vodafone.ecommerce.security.controller;

import com.vodafone.ecommerce.model.dto.CustomerRequestDTO;
import com.vodafone.ecommerce.model.dto.PasswordResetRequestDTO;
import com.vodafone.ecommerce.model.dto.UserRequestDTO;
import com.vodafone.ecommerce.model.dto.UserResponseDTO;
import com.vodafone.ecommerce.model.enums.TokenType;
import com.vodafone.ecommerce.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void registerUser(@Valid @RequestBody CustomerRequestDTO request) {
        authService.registerUser(request);
    }

    @PostMapping(path = "/confirm-account")
    @ResponseStatus(HttpStatus.OK)
    public void confirmUserAccount(@RequestParam("token") String confirmationToken, HttpServletResponse response) {
        authService.confirmEmail(confirmationToken);
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody UserRequestDTO authRequest, HttpServletResponse response) {
        UserResponseDTO userResponse = authService.login(authRequest, response);
        return ResponseEntity.ok(userResponse);
    }

    //For testing purposes only
    @PostMapping(path = "/FASTER-LOGIN", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "#@!^#&47#&@%&$#%&3547#&%$#&%$#& FASTER LOGIN #@!^#&47#&@%&$#%&3547#&%$#&%$#&")
    public String fasterLogin(@Valid @RequestBody UserRequestDTO authRequest, HttpServletResponse response) {
        UserResponseDTO userResponse = authService.login(authRequest, response);
        return userResponse.getToken();
    }

    @PostMapping("/request-password-reset")
    @ResponseStatus(HttpStatus.OK)
    public void requestPasswordReset(@RequestParam String email) {
        authService.sendPasswordResetEmail(email);
    }

    @GetMapping("/reset-password-form")
    @ResponseStatus(HttpStatus.OK)
    public void validateResetToken(@RequestParam("token") String token) {
        authService.validateResetToken(token, TokenType.PASSWORD_RESET);
    }

    @PostMapping(path = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        authService.resetPassword(request);
    }

    @PostMapping(
            path = "/refresh",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponseDTO> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        return ResponseEntity.ok(authService.refreshToken(cookies, response));
    }

    @PostMapping(path = "/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        authService.logout(cookies, response);
    }
}
