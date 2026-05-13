package com.services.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey key() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera el access token.
     * Subject: UUID del usuario (inmutable, nunca cambia aunque cambien email u otros datos).
     * Claims adicionales: email, roles.
     */
    public String generateAccessToken(UUID idUser, String email, List<String> roles, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(idUser.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("type", "access")
                .claim("email", email)
                .claim("roles", roles)
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Genera el refresh token.
     * Subject: UUID del usuario.
     */
    public String generateRefreshToken(UUID idUser, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(idUser.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    /** Retorna el UUID del usuario (subject). */
    public String getSubjectFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).get("email", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}