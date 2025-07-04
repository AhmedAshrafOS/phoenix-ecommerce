package com.vodafone.ecommerce.model.entity;

import com.vodafone.ecommerce.model.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "confirmationTokens")
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "token_id")
    private Long tokenId;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Column(name = "confirmation_token")
    private String token;

    @Column(name = "new_email")
    private String newEmail;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    @LastModifiedDate
    private LocalDate updatedDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ConfirmationToken(User user, TokenType tokenType) {
        this.user = user;
        this.tokenType = tokenType;
        this.createdDate = LocalDate.now();
        this.token = UUID.randomUUID().toString();
    }

}
