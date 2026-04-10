package com.memorator.service;

import com.memorator.dto.LoginRequest;
import com.memorator.dto.LoginResponse;
import com.memorator.dto.RegistrationRequest;
import com.memorator.dto.UserResponse;
import com.memorator.entity.RefreshToken;
import com.memorator.entity.User;
import com.memorator.exception.EmailAlreadyExistsException;
import com.memorator.exception.InvalidCredentialsException;
import com.memorator.exception.LoginAlreadyExistsException;
import com.memorator.repository.UserRepository;
import com.memorator.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    private User buildUser() {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .login("testuser")
                .password("encoded")
                .build();
    }

    private RefreshToken buildRefreshToken(User user) {
        return RefreshToken.builder()
                .id(1L)
                .user(user)
                .token("refresh-uuid")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }

    @Test
    void registerUser_success() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setLogin("testuser");
        req.setPassword("password");

        User user = buildUser();
        RefreshToken refreshToken = buildRefreshToken(user);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByLogin("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenService.create(any(User.class))).thenReturn(refreshToken);

        LoginResponse response = userService.registerUser(req);

        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
    }

    @Test
    void registerUser_emailAlreadyExists() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setLogin("testuser");
        req.setPassword("password");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(req))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void registerUser_loginAlreadyExists() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setLogin("testuser");
        req.setPassword("password");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByLogin("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(req))
                .isInstanceOf(LoginAlreadyExistsException.class);
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("testuser");
        req.setPassword("password");

        User user = buildUser();
        RefreshToken refreshToken = buildRefreshToken(user);

        when(userRepository.findByEmailOrLogin("testuser", "testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenService.create(user)).thenReturn(refreshToken);

        LoginResponse response = userService.login(req);

        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
    }

    @Test
    void login_userNotFound() {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("unknown");
        req.setPassword("password");

        when(userRepository.findByEmailOrLogin("unknown", "unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_wrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("testuser");
        req.setPassword("wrong");

        User user = buildUser();

        when(userRepository.findByEmailOrLogin("testuser", "testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void getUserById_success() {
        User user = User.builder()
                .id(1L)
                .login("testuser")
                .email("test@example.com")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLogin()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
