package com.example.cars.auth.infrastructure.adapter.out.security;

import com.example.cars.auth.application.port.out.TokenProviderPort;
import com.example.cars.users.domain.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** JWT implementation of the token provider port (HMAC-SHA, jjwt). */
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProviderAdapter(@Value("${app.jwt.secret}") String secret,
                                   @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(UserId userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId.value()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<UserId> validateAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(UserId.of(Long.parseLong(claims.getSubject())));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
