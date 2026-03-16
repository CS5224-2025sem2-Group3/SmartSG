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

    /**
     * 用户登录
     * @param request 登录请求（邮箱、密码）
     * @return 用户信息（不含密码）
     * @throws ResponseStatusException 如果邮箱不存在或密码错误
     */
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

    /**
     * 用户注册
     * @param request 注册请求（姓名、邮箱、密码）
     * @return 注册成功的用户信息
     * @throws ResponseStatusException 如果邮箱已存在
     */
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
}
