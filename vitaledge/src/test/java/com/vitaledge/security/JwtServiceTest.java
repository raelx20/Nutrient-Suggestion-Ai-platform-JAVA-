package com.vitaledge.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vitaledge.config.ApplicationProperties;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ApplicationProperties props = new ApplicationProperties();
        props.getSecurity().setSecretKey("unit-test-secret-key-0123456789abcdef01234567");
        props.getSecurity().setAccessTokenExpireMinutes(15);
        jwtService = new JwtService(props);
        jwtService.init();
    }

    @Test
    void createsAndDecodesAccessToken() {
        String token = jwtService.createAccessToken("user-123", List.of("CONSUMER"));
        Map<String, Object> claims = jwtService.decodeAccessToken(token);
        assertNotNull(claims);
        assertEquals("user-123", claims.get("sub"));
        assertEquals(List.of("CONSUMER"), claims.get("roles"));
        assertEquals(JwtService.TYPE_ACCESS, claims.get("type"));
    }

    @Test
    void rejectsWrongTokenType() {
        String refresh = jwtService.createRefreshToken("user-123");
        assertNull(jwtService.decodeAccessToken(refresh));
        assertNotNull(jwtService.decodeRefreshToken(refresh));
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.createAccessToken("user-123", List.of("CONSUMER"));
        String tampered = token.substring(0, token.length() - 4) + "AAAA";
        assertNull(jwtService.decodeAccessToken(tampered));
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        String token = jwtService.createAccessToken("user-123", List.of("CONSUMER"), 1);
        Thread.sleep(1500);
        assertNull(jwtService.decodeAccessToken(token));
    }

    @Test
    void hashTokenIsDeterministic() {
        String hash1 = JwtService.hashToken("abc-token");
        String hash2 = JwtService.hashToken("abc-token");
        assertEquals(hash1, hash2);
        assertTrue(hash1.length() == 64);
    }
}