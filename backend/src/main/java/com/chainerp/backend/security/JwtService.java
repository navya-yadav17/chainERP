package com.chainerp.backend.security;

import com.chainerp.backend.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final SecurityProperties properties;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
    }

    public String generateToken(UserDetails user) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(properties.getAccessTokenMinutes() * 60);
        String[] roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toArray(String[]::new);
        return Jwts.builder()
            .subject(user.getUsername())
            .claims(Map.of("roles", roles))
            .issuedAt(Date.from(now))
            .expiration(Date.from(expires))
            .signWith(key())
            .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token, UserDetails user) {
        Claims claims = parseClaims(token);
        return user.getUsername().equals(claims.getSubject())
            && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(key())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey key() {
        byte[] secret = properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("CHAINERP_JWT_SECRET must contain at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(secret);
    }
}
