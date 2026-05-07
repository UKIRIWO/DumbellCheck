package com.agg.dumbellcheck.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import com.agg.dumbellcheck.config.SecurityProperties;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecurityProperties securityProperties;

    public JwtService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String generateAccessToken(String username, Map<String, Object> claims) {
        return generateToken(username, claims, securityProperties.jwtExpirationSeconds(), ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, Map.of(), securityProperties.jwtRefreshExpirationSeconds(), REFRESH_TOKEN_TYPE);
    }

    public String generateToken(String username, Map<String, Object> claims, long expirationSeconds, String tokenType) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);
        Map<String, Object> tokenClaims = new HashMap<>(claims);
        tokenClaims.put(TOKEN_TYPE_CLAIM, tokenType);

        return Jwts.builder()
                .claims(tokenClaims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSignInKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username, String expectedTokenType) {
        Claims claims = extractAllClaims(token);
        String tokenUsername = extractUsername(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        return tokenUsername.equals(username)
                && expectedTokenType.equals(tokenType)
                && !claims.getExpiration().before(new Date());
    }

    public boolean isAccessTokenValid(String token, String username) {
        return isTokenValid(token, username, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshTokenValid(String token, String username) {
        return isTokenValid(token, username, REFRESH_TOKEN_TYPE);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(securityProperties.jwtSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
