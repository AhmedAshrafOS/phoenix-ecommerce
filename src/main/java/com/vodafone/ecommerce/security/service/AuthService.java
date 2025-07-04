package com.vodafone.ecommerce.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.vodafone.ecommerce.exception.InvalidTokenException;
import com.vodafone.ecommerce.model.dto.CustomerRequestDTO;
import com.vodafone.ecommerce.model.dto.PasswordResetRequestDTO;
import com.vodafone.ecommerce.model.dto.UserRequestDTO;
import com.vodafone.ecommerce.model.dto.UserResponseDTO;
import com.vodafone.ecommerce.model.entity.ConfirmationToken;
import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.model.enums.AccountStatus;
import com.vodafone.ecommerce.model.enums.Role;
import com.vodafone.ecommerce.model.enums.TokenType;
import com.vodafone.ecommerce.repository.ConfirmationTokenRepository;
import com.vodafone.ecommerce.repository.UserRepository;
import com.vodafone.ecommerce.security.model.entity.RefreshToken;
import com.vodafone.ecommerce.security.repository.RefreshTokenRepository;
import com.vodafone.ecommerce.service.impl.EmailServiceImpl;
import com.vodafone.ecommerce.service.impl.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN = "refreshToken";
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailServiceImpl emailService;
    private final UserServiceImpl userService;
    private final Cache<String, Integer> loginFailureCache;

    public UserResponseDTO login(UserRequestDTO request, HttpServletResponse response) {
        String emailOrUserName = request.getUsernameOrEmail().toLowerCase();
        log.info("Login attempt for: {}", emailOrUserName);

        User user = userRepository.findByUsernameOrEmailIgnoreCase(emailOrUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (user.getAccountStatus() == AccountStatus.DEACTIVATED) {
            log.warn("Unverified account login attempt: {}", emailOrUserName);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email before logging in.");
        }

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            log.warn("Suspended account login attempt: {}", emailOrUserName);
            throw new ResponseStatusException(HttpStatus.LOCKED, "Account suspended. Please reset your password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user, emailOrUserName);
        }

        if (user.getRole() == Role.CUSTOMER) {
            loginFailureCache.invalidate(emailOrUserName);
        }

        log.info("Login successful for: {}", emailOrUserName);
        return buildResponse(user, response);
    }

    private void handleFailedLogin(User user, String emailOrUserName) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        int attempts = loginFailureCache.getIfPresent(emailOrUserName) == null ? 0 : loginFailureCache.getIfPresent(emailOrUserName);
        attempts++;

        if (attempts >= 3) {
            user.setAccountStatus(AccountStatus.SUSPENDED);
            userRepository.save(user);
            loginFailureCache.invalidate(emailOrUserName);
            throw new ResponseStatusException(HttpStatus.LOCKED, "Account suspended. Please reset your password.");
        } else {
            loginFailureCache.put(emailOrUserName, attempts);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }
        ConfirmationToken token = new ConfirmationToken(user, TokenType.PASSWORD_RESET);
        confirmationTokenRepository.save(token);
        emailService.generateEmail(user.getEmail(), token);

        log.info("Password reset email sent to: {}", email);
    }

    public UserResponseDTO refreshToken(Cookie[] cookies, HttpServletResponse response) {
        String refreshToken = CookieService.getCookieValue(cookies, REFRESH_TOKEN);

        System.out.println("Refresh Token: " + refreshToken);

        if (refreshToken == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token not found in cookies");

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new InvalidTokenException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (jwtService.isRefreshTokenExpired(storedToken))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token expired");

        if (storedToken.isRevoked())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token has been revoked");

        User user = storedToken.getUser();
        refreshTokenRepository.delete(storedToken);
        log.info("Refresh token used and deleted for user: {}", user.getUsername());

        return buildResponse(user, response);
    }

    public void logout(Cookie[] cookies, HttpServletResponse response) {
        String refreshToken = CookieService.getCookieValue(cookies, REFRESH_TOKEN);

        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found in cookies");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new InvalidTokenException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        refreshTokenRepository.delete(storedToken);
        CookieService.clearCookie(response, REFRESH_TOKEN);
        log.info("User logged out and refresh token deleted.");
    }

    private UserResponseDTO buildResponse(User user, HttpServletResponse response) {
        String newAccessToken = jwtService.createToken(user);
        RefreshToken newRefreshToken = jwtService.createRefreshToken(user);
        LocalDateTime expiry = jwtService.getExpiryFromToken(newAccessToken);

        response.setHeader("Authorization", "Bearer " + newAccessToken);

        int maxAgeSeconds = (int) ((newRefreshToken.getExpiryDate().getTime() - new Date().getTime()) / 1000);
        System.out.println("Max age for refresh token: " + maxAgeSeconds + " seconds");
        CookieService.addHttpOnlyCookie(response, REFRESH_TOKEN, newRefreshToken.getToken(), maxAgeSeconds);

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setToken(newAccessToken);
        userResponseDTO.setExpiry(expiry);
        return userResponseDTO;
    }

    public void registerUser(CustomerRequestDTO request) {
        User user = userService.registerUser(request);
        ConfirmationToken confirmationToken = new ConfirmationToken(user, TokenType.EMAIL_VERIFICATION);

        confirmationTokenRepository.save(confirmationToken);

        emailService.generateEmail(user.getEmail(), confirmationToken);
    }

    public void confirmEmail(String confirmationToken) {
        ConfirmationToken token = confirmationTokenRepository.findByToken(confirmationToken);

        if (token == null) {
            throw new InvalidTokenException("Invalid or expired confirmation token");
        }

        User user = userRepository.findByEmailIgnoreCase(token.getUser().getEmail());

        if (token.getNewEmail() != null) user.setEmail(token.getNewEmail());

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        confirmationTokenRepository.delete(token);

    }

    public void resetPassword(PasswordResetRequestDTO passwordResetRequest) {
        ConfirmationToken token = validateResetToken(passwordResetRequest.getToken(), TokenType.PASSWORD_RESET);

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(passwordResetRequest.getNewPassword()));
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        confirmationTokenRepository.delete(token);
    }

    public ConfirmationToken validateResetToken(String tokenValue, TokenType tokenType) {
        ConfirmationToken token = confirmationTokenRepository.findByToken(tokenValue);
        if (token == null || !token.getTokenType().equals(tokenType)) {
            throw new InvalidTokenException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }
        return token;
    }
}
