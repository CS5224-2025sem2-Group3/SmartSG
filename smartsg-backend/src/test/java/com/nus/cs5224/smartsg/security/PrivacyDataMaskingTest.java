package com.nus.cs5224.smartsg.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Privacy Data Masking Tests for SmartSG Backend
 *
 * Verifies the privacy-first data masking architecture via real HTTP calls:
 *  - Listing responses must not expose exact street addresses
 *  - Listing search results must not leak password hashes or email addresses
 *
 * Note: PII masking via @JsonInclude(NON_NULL) on UserResponse is a Jackson
 * framework feature and does not require dedicated test coverage here.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("security")
class PrivacyDataMaskingTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Listing detail response must not expose an exact street address string.
     * Coordinates (latitude/longitude) or distance metrics are acceptable.
     */
    @Test
    void listingDetail_ShouldNotExposeExactAddress() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/listings/1"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();

        assertFalse(body.contains("\"address\""),
                "Exact address string must not appear in listing detail response");

        assertTrue(
                body.contains("latitude") || body.contains("distance") || body.contains("location"),
                "Response should contain coordinate or distance fields instead of a full address");
    }

    /**
     * Listing search results must never expose sensitive user data
     * (password hashes, email addresses) that originate from the User table.
     */
    @Test
    void listingSearch_ShouldNotExposeSensitiveUserData() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/listings")
                        .param("budgetMax", "5000"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();

        assertFalse(body.toLowerCase().contains("passwordhash"),
                "Password hash must never appear in listing search results");
        assertFalse(body.toLowerCase().contains("\"password\""),
                "Password field must not appear in listing search results");
        assertFalse(body.toLowerCase().contains("\"email\""),
                "Email field must not be exposed in listing search results");
    }
}
