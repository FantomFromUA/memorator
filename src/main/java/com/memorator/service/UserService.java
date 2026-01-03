package com.memorator.service;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.memorator.dto.LoginRequest;
import com.memorator.dto.LoginResponse;
import com.memorator.dto.RegistrationRequest;
import com.memorator.entity.RefreshToken;
import com.memorator.entity.User;
import com.memorator.exception.EmailAlreadyExistsException;
import com.memorator.exception.InvalidCredentialsException;
import com.memorator.repository.UserRepository;
import com.memorator.security.JwtService;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse registerUser(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }
}
