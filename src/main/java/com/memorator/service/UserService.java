package com.memorator.service;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.memorator.dto.RegistrationRequest;
import com.memorator.dto.LoginRequest;
import com.memorator.dto.UserResponse;
import com.memorator.entity.User;
import com.memorator.repository.UserRepository;

import com.memorator.exception.EmailAlreadyExistsException;
import com.memorator.exception.InvalidCredentialsException;
import com.memorator.security.JwtService;



@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse registerUser(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getCreatedAt(),
            token
        );
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getCreatedAt(),
            token
        );
    }
}
