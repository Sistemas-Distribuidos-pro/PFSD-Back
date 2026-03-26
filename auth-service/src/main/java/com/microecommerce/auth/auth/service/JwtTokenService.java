package com.microecommerce.auth.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {

    private final String jwtSecret;
    private SecretKey secretKey;

    @Value("${auth.jwt.expiration-seconds}")
    private long expirationSeconds;

    public JwtTokenService(@Value("${auth.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException("auth.jwt.secret debe tener al menos 32 caracteres");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String email, String provider, String externalSubject) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("provider", provider)
                .claim("external_sub", externalSubject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
