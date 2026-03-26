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

    // login
    @Override
    public UserResponse login(LoginRequest request) {
        User user = authMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail());
    }

    // register new user
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 检查邮箱是否已存在
        User existingUser = authMapper.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // 创建新用户对象
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 插入数据库
        authMapper.insertUser(newUser);

        return new UserResponse(newUser.getUserId(), newUser.getName(), newUser.getEmail());
    }

    // get user by id
    @Override
    public UserResponse getUserById(Long userId) {
        User user = authMapper.findById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail());
    }
}
