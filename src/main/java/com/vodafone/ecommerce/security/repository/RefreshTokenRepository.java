package com.vodafone.ecommerce.security.repository;

import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.security.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
