package com.nus.cs5224.smartsg.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil — no Spring context or database required.
 *
 * Coverage:
 *  1. Token generation and claim extraction (userId, email)
 *  2. validateToken — valid / tampered / expired / wrong-secret / malformed / empty
 *  3. getUserIdFromToken — correct round-trip
 */
@Tag("security")
class JwtUtilTest {

    private static final String SECRET =
            "smartsg-cs5224-jwt-secret-key-2024-this-needs-to-be-at-least-256-bits-long";
    private static final long EXPIRATION_24H = 86400000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_24H);
    }

    // ===== 1. Token Generation & Claim Extraction =====

    @Test
    void generateToken_ShouldReturnNonBlankString() {
        String token = jwtUtil.generateToken(1L, "user@example.com");
        assertNotNull(token, "token must not be null");
        assertFalse(token.isBlank(), "token must not be blank");
    }

    @Test
    void generateToken_ShouldEmbedCorrectUserId() {
        String token = jwtUtil.generateToken(42L, "user@example.com");
        assertEquals(42L, jwtUtil.getUserIdFromToken(token),
                "userId extracted from token must match the one used at generation");
    }

    @Test
    void generateToken_ShouldEmbedCorrectEmail() {
        String token = jwtUtil.generateToken(1L, "alice@nus.edu.sg");
        Claims claims = jwtUtil.parseToken(token);
        assertEquals("alice@nus.edu.sg", claims.get("email", String.class),
                "email claim must match");
    }

    @Test
    void generateToken_DifferentUsers_ShouldProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken(1L, "a@test.com");
        String token2 = jwtUtil.generateToken(2L, "b@test.com");
        assertNotEquals(token1, token2,
                "tokens for different users must differ");
    }

    // ===== 2. validateToken =====

    @Test
    void validateToken_FreshToken_ShouldReturnTrue() {
        String token = jwtUtil.generateToken(10L, "valid@test.com");
        assertTrue(jwtUtil.validateToken(token), "a freshly generated token must be valid");
    }

    @Test
    void validateToken_TamperedSignature_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(10L, "valid@test.com");

        // Flip last character of the signature (3rd JWT segment)
        String[] parts = token.split("\\.");
        String sig = parts[2];
        String tamperedSig = sig.substring(0, sig.length() - 1)
                + (sig.endsWith("a") ? "b" : "a");
        String tampered = parts[0] + "." + parts[1] + "." + tamperedSig;

        assertFalse(jwtUtil.validateToken(tampered), "tampered token must be rejected");
    }

    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        // Negative expiration → token is already expired when created
        JwtUtil expiredUtil = new JwtUtil(SECRET, -1000L);
        String expiredToken = expiredUtil.generateToken(5L, "expired@test.com");
        assertFalse(jwtUtil.validateToken(expiredToken), "expired token must be rejected");
    }

    @Test
    void validateToken_ExpiredToken_ShortLived_ShouldReturnFalse() throws InterruptedException {
        JwtUtil shortLived = new JwtUtil(SECRET, 1L); // 1 ms
        String token = shortLived.generateToken(1L, "test@example.com");
        Thread.sleep(20);
        assertFalse(jwtUtil.validateToken(token), "token expired after sleep must be rejected");
    }

    @Test
    void validateToken_TokenSignedWithDifferentSecret_ShouldReturnFalse() {
        JwtUtil otherUtil = new JwtUtil(
                "completely-different-secret-key-that-is-also-long-enough-for-hmac256",
                EXPIRATION_24H);
        String foreignToken = otherUtil.generateToken(7L, "foreign@test.com");
        assertFalse(jwtUtil.validateToken(foreignToken),
                "token signed with a different key must fail validation");
    }

    @Test
    void validateToken_RandomString_ShouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("this.is.not.a.jwt"),
                "random string must not be a valid token");
    }

    @Test
    void validateToken_EmptyString_ShouldReturnFalse() {
        assertFalse(jwtUtil.validateToken(""),
                "empty string must not be a valid token");
    }

    // ===== 3. getUserIdFromToken round-trip =====

    @Test
    void getUserIdFromToken_ShouldMatchOriginalId() {
        long originalId = 1234L;
        String token = jwtUtil.generateToken(originalId, "match@test.com");
        assertEquals(originalId, jwtUtil.getUserIdFromToken(token),
                "round-trip userId must equal original");
    }
}
