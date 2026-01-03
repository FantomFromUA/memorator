package com.memorator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memorator.dto.LoginRequest;
import com.memorator.dto.LoginResponse;
import com.memorator.dto.LogoutRequest;
import com.memorator.dto.RefreshRequest;
import com.memorator.dto.RegistrationRequest;
import com.memorator.entity.RefreshToken;
import com.memorator.entity.User;
import com.memorator.repository.RefreshTokenRepository;
import com.memorator.security.JwtService;
import com.memorator.service.RefreshTokenService;
import com.memorator.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegistrationRequest request) {
        LoginResponse user = userService.registerUser(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse user = userService.login(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.validate(request.getRefreshToken());

        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(
                new LoginResponse(newAccessToken, refreshToken.getToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {

        RefreshToken token =
                refreshTokenService.validate(request.getRefreshToken());

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return ResponseEntity.noContent().build();
    }
}
