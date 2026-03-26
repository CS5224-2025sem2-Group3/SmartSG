package com.nus.cs5224.smartsg.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for JwtUtil - no Spring context or database needed.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Directly instantiate JwtUtil with test config (no Spring needed)
        String secret = "smartsg-cs5224-jwt-secret-key-2024-this-needs-to-be-at-least-256-bits-long";
        long expiration = 86400000L; // 24 hours
        jwtUtil = new JwtUtil(secret, expiration);
    }

    // test generate and parse token
    @Test
    void testGenerateAndParseToken() {
        Long userId = 42L;
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, email);

        assertNotNull(token);
        System.out.println("Generated token: " + token);

        Long extractedUserId = jwtUtil.getUserIdFromToken(token);
        assertEquals(userId, extractedUserId);
        System.out.println("Extracted userId: " + extractedUserId);
    }

    // test validate token
    @Test
    void testValidateToken_validToken() {
        String token = jwtUtil.generateToken(1L, "test@example.com");
        assertTrue(jwtUtil.validateToken(token));
        System.out.println("Token is valid: " + jwtUtil.validateToken(token));
    }

    // test invalid token
    @Test
    void testValidateToken_invalidToken() {
        String fakeToken = "this.is.not.a.valid.token";
        assertFalse(jwtUtil.validateToken(fakeToken));
        System.out.println("Fake token is valid: " + jwtUtil.validateToken(fakeToken));
    }

    // test expired token
    @Test
    void testValidateToken_expiredToken() throws InterruptedException {
        // Create a JwtUtil with 1ms expiration
        JwtUtil shortLivedJwt = new JwtUtil(
                "smartsg-cs5224-jwt-secret-key-2024-this-needs-to-be-at-least-256-bits-long",
                1L // 1 millisecond
        );
        String token = shortLivedJwt.generateToken(1L, "test@example.com");
        Thread.sleep(10); // wait for token to expire
        assertFalse(shortLivedJwt.validateToken(token));
        System.out.println("Expired token is valid: " + shortLivedJwt.validateToken(token));
    }
}
