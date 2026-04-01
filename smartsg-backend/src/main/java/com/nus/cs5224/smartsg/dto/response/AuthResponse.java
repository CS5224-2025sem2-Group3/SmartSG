package com.nus.cs5224.smartsg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// response for login status to frontend
public class AuthResponse {
    private String token;
    private UserResponse user;
}
