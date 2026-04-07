package com.nus.cs5224.smartsg.security;

import com.nus.cs5224.smartsg.util.JwtUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JwtAuthenticationFilter
 *
 * Verifies that:
 *  1. Protected endpoints return 401 when no token is provided
 *  2. Protected endpoints return 401 when the token is invalid / tampered
 *  3. Protected endpoints return 401 when Authorization header format is wrong
 *  4. Public endpoints (login, register, GET listings, universities) pass through without a token
 *  5. CORS OPTIONS pre-flight requests pass through without a token
 *  6. Valid JWT grants access to protected endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("security")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    // ===== 1. Protected endpoints — no token =====

    @Test
    void protectedEndpoint_NoToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_NoToken_Groups_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/groups/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_NoToken_Profile_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_NoToken_Favorites_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isUnauthorized());
    }

    // ===== 2. Protected endpoints — invalid / tampered token =====

    @Test
    void protectedEndpoint_InvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer this.is.not.a.valid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_TamperedToken_ShouldReturn401() throws Exception {
        String validToken = jwtUtil.generateToken(1L, "user@test.com");

        // Tamper the signature (flip last character)
        String[] parts = validToken.split("\\.");
        String sig = parts[2];
        String tamperedSig = sig.substring(0, sig.length() - 1)
                + (sig.endsWith("a") ? "b" : "a");
        String tampered = parts[0] + "." + parts[1] + "." + tamperedSig;

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    // ===== 3. Wrong Authorization header format =====

    @Test
    void protectedEndpoint_MissingBearerPrefix_ShouldReturn401() throws Exception {
        String token = jwtUtil.generateToken(1L, "user@test.com");
        // Header present but missing "Bearer " prefix
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_EmptyBearerToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    // ===== 4. Public endpoints — no token required =====

    @Test
    void publicEndpoint_Login_ShouldNotReturn401() throws Exception {
        // Login endpoint is public; it must not be blocked by the filter.
        // We don't assert 200 (the request body is empty so it may be 400/500),
        // but it must NOT be 401.
        int status = mockMvc.perform(post("/api/auth/login"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "/api/auth/login must not be blocked by JWT filter");
    }

    @Test
    void publicEndpoint_Register_ShouldNotReturn401() throws Exception {
        int status = mockMvc.perform(post("/api/auth/register"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "/api/auth/register must not be blocked by JWT filter");
    }

    @Test
    void publicEndpoint_GetListings_ShouldNotReturn401() throws Exception {
        // GET /api/listings is a public read endpoint
        int status = mockMvc.perform(get("/api/listings"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "GET /api/listings must be publicly accessible");
    }

    @Test
    void publicEndpoint_GetUniversities_ShouldNotReturn401() throws Exception {
        int status = mockMvc.perform(get("/api/universities"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "GET /api/universities must be publicly accessible");
    }

    // ===== 5. CORS OPTIONS pre-flight =====

    @Test
    void optionsRequest_ShouldPassThroughWithoutToken() throws Exception {
        // CORS pre-flight must never be blocked by auth
        int status = mockMvc.perform(options("/api/groups/me"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "OPTIONS pre-flight must not return 401");
    }

    // ===== 6. Valid JWT grants access =====

    @Test
    void protectedEndpoint_ValidToken_ShouldNotReturn401() throws Exception {
        // Generate a valid token; the endpoint may return 404/500 due to missing DB data,
        // but it must NOT be 401 (filter should let it through).
        String token = jwtUtil.generateToken(999999L, "testuser@test.com");

        int status = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();

        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "Valid JWT must not be rejected by the filter");
    }
}
