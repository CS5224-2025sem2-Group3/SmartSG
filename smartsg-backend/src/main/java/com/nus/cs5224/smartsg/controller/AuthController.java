package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.request.LoginRequest;
import com.nus.cs5224.smartsg.dto.request.RegisterRequest;
import com.nus.cs5224.smartsg.dto.response.AuthResponse;
import com.nus.cs5224.smartsg.dto.response.UserResponse;
import com.nus.cs5224.smartsg.service.AuthService;
import com.nus.cs5224.smartsg.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Login: validate credentials and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        UserResponse user = authService.login(request);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    /**
     * Register: create new user and return JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, user));
    }

    /**
     * Logout: JWT is stateless, client just discards the token
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current logged-in user info (from JWT token)
     * Returns 401 if no valid token (handled by JwtAuthenticationFilter)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse user = authService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}
