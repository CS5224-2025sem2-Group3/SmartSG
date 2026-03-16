package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.LoginRequest;
import com.nus.cs5224.smartsg.dto.request.RegisterRequest;
import com.nus.cs5224.smartsg.dto.response.UserResponse;

public interface AuthService {
    public UserResponse login(LoginRequest request);
    public UserResponse register(RegisterRequest request);
}
