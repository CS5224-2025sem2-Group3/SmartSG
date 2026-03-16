package com.nus.cs5224.smartsg.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
