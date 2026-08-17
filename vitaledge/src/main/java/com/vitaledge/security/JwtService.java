package com.vitaledge.security;

import com.vitaledge.config.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * JWT creation and validation. Ports {@code utils/security.py} from the source project,
 * including the explicit access/refresh token-type separation that prevents using a
 * refresh token as an access token (and vice versa).
 */
@Component
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String TYPE_EMAIL_VERIFY = "email_verify";

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final ApplicationProperties properties;
    private SecretKey signingKey;

    public JwtService(ApplicationProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        String secret = properties.getSecurity().getSecretKey();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("SECRET_KEY must be at least 32 characters long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(properties.getSecurity().getAccessTokenExpireMinutes() * 60L);
        return buildToken(subject, roles, TYPE_ACCESS, now, expires);
    }

    public String createAccessToken(String subject, List<String> roles, int ttlSeconds) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(ttlSeconds);
        return buildToken(subject, roles, TYPE_ACCESS, now, expires);
    }

    public String createRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(properties.getSecurity().getRefreshTokenExpireDays() * 86400L);
        return buildToken(subject, List.of(), TYPE_REFRESH, now, expires);
    }

    public String createEmailVerificationToken(String subject) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(properties.getSecurity().getEmailVerificationExpireHours() * 3600L);
        return buildToken(subject, List.of(), TYPE_EMAIL_VERIFY, now, expires);
    }

    private String buildToken(String subject, List<String> roles, String type, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .claim("type", type)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Decode and validate a JWT, requiring the expected token type.
     * Explicitly rejects tokens of the wrong type (e.g. a refresh token used as access token).
     *
     * @return claims payload, or null when invalid
     */
    public Map<String, Object> decode(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            if (!expectedType.equals(tokenType)) {
                log.warn("Token rejected: expected type={}, got type={}", expectedType, tokenType);
                return null;
            }
            return claims;
        } catch (Exception e) {
            log.warn("JWT decode error: {}", e.getMessage());
            return null;
        }
    }

    public Map<String, Object> decodeAccessToken(String token) {
        return decode(token, TYPE_ACCESS);
    }

    public Map<String, Object> decodeRefreshToken(String token) {
        return decode(token, TYPE_REFRESH);
    }

    public Map<String, Object> decodeEmailVerificationToken(String token) {
        return decode(token, TYPE_EMAIL_VERIFY);
    }

    /** Hash a raw token for at-rest storage and blacklist keys. */
    public static String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}