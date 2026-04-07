package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.LoginRequest;
import com.nus.cs5224.smartsg.dto.request.RegisterRequest;
import com.nus.cs5224.smartsg.dto.response.UserResponse;
import com.nus.cs5224.smartsg.entity.User;
import com.nus.cs5224.smartsg.mapper.AuthMapper;
import com.nus.cs5224.smartsg.service.serviceImpl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setName("Alice");
        existingUser.setEmail("alice@test.com");
        existingUser.setPasswordHash(encoder.encode("password123"));
    }

    // ========== Login Tests ==========

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("A6: 正常登录")
        void login_success() {
            LoginRequest request = new LoginRequest();
            request.setEmail("Alice@TEST.com");
            request.setPassword("password123");

            when(authMapper.findByEmail("alice@test.com")).thenReturn(existingUser);

            UserResponse response = authService.login(request);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Alice", response.getName());
            assertEquals("alice@test.com", response.getEmail());
        }

        @Test
        @DisplayName("A7: 密码错误 - 返回401")
        void login_wrongPassword() {
            LoginRequest request = new LoginRequest();
            request.setEmail("alice@test.com");
            request.setPassword("wrongpassword");

            when(authMapper.findByEmail("alice@test.com")).thenReturn(existingUser);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login(request));

            assertEquals(401, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Invalid email or password"));
        }

        @Test
        @DisplayName("A8: 邮箱不存在 - 返回401")
        void login_emailNotFound() {
            LoginRequest request = new LoginRequest();
            request.setEmail("notexist@test.com");
            request.setPassword("password123");

            when(authMapper.findByEmail("notexist@test.com")).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login(request));

            assertEquals(401, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("A9: 邮箱大小写不敏感登录")
        void login_emailCaseInsensitive() {
            LoginRequest request = new LoginRequest();
            request.setEmail("  ALICE@TEST.COM  ");
            request.setPassword("password123");

            when(authMapper.findByEmail("alice@test.com")).thenReturn(existingUser);

            UserResponse response = authService.login(request);

            assertNotNull(response);
            assertEquals(1L, response.getId());
        }
    }

    // ========== Register Tests ==========

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("A1: 正常注册")
        void register_success() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Bob");
            request.setEmail("bob@test.com");
            request.setPassword("password456");

            when(authMapper.findByEmail("bob@test.com")).thenReturn(null);
            when(authMapper.insertUser(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setUserId(2L);
                return 1;
            });

            UserResponse response = authService.register(request);

            assertNotNull(response);
            assertEquals(2L, response.getId());
            assertEquals("Bob", response.getName());
            assertEquals("bob@test.com", response.getEmail());
        }

        @Test
        @DisplayName("A2: 邮箱重复注册 - 返回409")
        void register_duplicateEmail() {
            RegisterRequest request = new RegisterRequest();
            request.setName("AliceDup");
            request.setEmail("alice@test.com");
            request.setPassword("password123");

            when(authMapper.findByEmail("alice@test.com")).thenReturn(existingUser);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.register(request));

            assertEquals(409, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Email already registered"));
        }

        @Test
        @DisplayName("A3: 邮箱大小写不敏感 - 重复注册返回409")
        void register_emailCaseInsensitive() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Alice2");
            request.setEmail("ALICE@TEST.COM");
            request.setPassword("password123");

            when(authMapper.findByEmail("alice@test.com")).thenReturn(existingUser);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.register(request));

            assertEquals(409, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("A4: 邮箱自动trim")
        void register_emailTrimmed() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Trimmed");
            request.setEmail("  bob@test.com  ");
            request.setPassword("password123");

            when(authMapper.findByEmail("bob@test.com")).thenReturn(null);
            when(authMapper.insertUser(any(User.class))).thenReturn(1);

            UserResponse response = authService.register(request);

            assertNotNull(response);
            verify(authMapper).findByEmail("bob@test.com");
        }
    }

    // ========== Get User By ID Tests ==========

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("A10: 获取存在的用户")
        void getUserById_success() {
            when(authMapper.findById(1L)).thenReturn(existingUser);

            UserResponse response = authService.getUserById(1L);

            assertEquals(1L, response.getId());
            assertEquals("Alice", response.getName());
        }

        @Test
        @DisplayName("用户不存在 - 返回404")
        void getUserById_notFound() {
            when(authMapper.findById(999L)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.getUserById(999L));

            assertEquals(404, ex.getStatusCode().value());
        }
    }
}
