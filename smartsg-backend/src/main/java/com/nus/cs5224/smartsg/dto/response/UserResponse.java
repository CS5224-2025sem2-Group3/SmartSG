package com.nus.cs5224.smartsg.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * UserResponse - Contains user identity information (PII)
 * 
 * Privacy Control: @JsonInclude(NON_NULL)
 * - When name or email are null (pre-confirmation), they are EXCLUDED from JSON response
 * - Client never receives null fields - they're completely absent from response
 * - Example pre-confirmation: {"id": 123}
 * - Example post-confirmation: {"id": 123, "name": "John Doe", "email": "john@example.com"}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class UserResponse {
    private Long id;
    private String name;
    private String email;
}