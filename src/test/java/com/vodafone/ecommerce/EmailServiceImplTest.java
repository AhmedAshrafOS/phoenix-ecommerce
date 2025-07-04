package com.vodafone.ecommerce;

import com.vodafone.ecommerce.model.entity.ConfirmationToken;
import com.vodafone.ecommerce.model.enums.TokenType;
import com.vodafone.ecommerce.service.AsyncEmailSender;
import com.vodafone.ecommerce.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private AsyncEmailSender asyncEmailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private ConfirmationToken emailVerificationToken;
    private ConfirmationToken passwordResetToken;

    @BeforeEach
    void setUp() {
        emailVerificationToken = new ConfirmationToken();
        emailVerificationToken.setToken("verify-token-123");
        emailVerificationToken.setTokenType(TokenType.EMAIL_VERIFICATION);

        passwordResetToken = new ConfirmationToken();
        passwordResetToken.setToken("reset-token-456");
        passwordResetToken.setTokenType(TokenType.PASSWORD_RESET);
    }

    @Test
    void generateLinkForToken_shouldReturnEmailVerificationUrl() {
        String link = emailService.generateLinkForToken(emailVerificationToken);
        assertThat(link).isEqualTo("https://ecommerce-phoenix.netlify.app/verifyemail?token=verify-token-123");
    }

    @Test
    void generateLinkForToken_shouldReturnPasswordResetUrl() {
        String link = emailService.generateLinkForToken(passwordResetToken);
        assertThat(link).isEqualTo("http://localhost:8085/reset-password-form?token=reset-token-456");
    }

    @Test
    void generateEmail_shouldBuildEmailCorrectly_forVerification() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.generateEmail("test@example.com", emailVerificationToken);

        verify(asyncEmailSender).sendEmail(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("test@example.com");
        assertThat(sent.getSubject()).isEqualTo("Verify Your Email!");
        assertThat(sent.getText()).contains("http://localhost:5173/verifyemail?token=verify-token-123");
    }

    @Test
    void generateEmail_shouldBuildEmailCorrectly_forPasswordReset() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.generateEmail("reset@example.com", passwordResetToken);

        verify(asyncEmailSender).sendEmail(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("reset@example.com");
        assertThat(sent.getSubject()).isEqualTo("Reset Your Password");
        assertThat(sent.getText()).contains("http://localhost:8085/reset-password-form?token=reset-token-456");
    }
}