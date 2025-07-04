package com.vodafone.ecommerce.service.impl;

import com.vodafone.ecommerce.model.entity.ConfirmationToken;
import com.vodafone.ecommerce.model.enums.TokenType;
import com.vodafone.ecommerce.service.AsyncEmailSender;
import com.vodafone.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final AsyncEmailSender asyncEmailSender;

    public void generateEmail(String to, ConfirmationToken token) {
        String link = generateLinkForToken(token);
        String subject = token.getTokenType() == TokenType.PASSWORD_RESET
                ? "Reset Your Password"
                : "Verify Your Email!";

        String message = token.getTokenType() == TokenType.PASSWORD_RESET
                ? "To reset your password, please click here: " + link
                : "To confirm your account, please click here: " + link;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        asyncEmailSender.sendEmail(mailMessage);
    }

    public String generateLinkForToken(ConfirmationToken token) {
        return switch (token.getTokenType()) {
            case EMAIL_VERIFICATION -> "https://ecommerce-phoenix.netlify.app/verifyemail?token=" + token.getToken();
            case PASSWORD_RESET -> "https://ecommerce-phoenix.netlify.app/reset-password?token=" + token.getToken();
        };
    }
}
