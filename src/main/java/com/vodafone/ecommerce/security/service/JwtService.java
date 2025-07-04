package com.vodafone.ecommerce.security.service;

import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.security.model.entity.RefreshToken;
import com.vodafone.ecommerce.security.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {

    public static final String ROLE_PREFIX = "ROLE_";
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh.expiration}")
    private long refreshTokenValidity;

    @Value("${security.jwt.access.expiration}")
    private long accessTokenValidity;

    @Value("${security.jwt.secret-key}")
    private String secret;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secret.getBytes()));
    }

    public String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put(CLAIM_USER_ID, user.getUserId());
        claims.put(CLAIM_ROLE, ROLE_PREFIX + user.getRole().name());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public RefreshToken createRefreshToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put(CLAIM_USER_ID, user.getUserId());
        claims.put(CLAIM_ROLE, ROLE_PREFIX + user.getRole().name());
        claims.put(CLAIM_TYPE, "refresh");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidity);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(expiry);

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public LocalDateTime getExpiryFromToken(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public boolean isRefreshTokenExpired(RefreshToken token) {
        return token.getExpiryDate().before(new Date());
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    public String getUsername(Claims claims) {
        return claims.getSubject();
    }

    public String getRole(Claims claims) {
        return (String) claims.get(CLAIM_ROLE);
    }

    public Long getUserIdFromToken(Claims claims) {
        return ((Number) claims.get(CLAIM_USER_ID)).longValue();
    }
}
