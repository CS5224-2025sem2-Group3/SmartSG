package com.nus.cs5224.smartsg.service.serviceImpl;

import com.nus.cs5224.smartsg.dto.request.LoginRequest;
import com.nus.cs5224.smartsg.dto.request.RegisterRequest;
import com.nus.cs5224.smartsg.dto.response.UserResponse;
import com.nus.cs5224.smartsg.entity.User;
import com.nus.cs5224.smartsg.mapper.AuthMapper;
import com.nus.cs5224.smartsg.service.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthMapper authMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = authMapper.findByEmail(normalizedEmail);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail());
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 检查邮箱是否已存在
        User existingUser = authMapper.findByEmail(normalizedEmail);
        if (existingUser != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // 创建新用户对象
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(normalizedEmail);
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 插入数据库
        authMapper.insertUser(newUser);

        return new UserResponse(newUser.getUserId(), newUser.getName(), newUser.getEmail());
    }
}
