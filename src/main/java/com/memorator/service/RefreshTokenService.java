package com.memorator.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.memorator.entity.RefreshToken;
import com.memorator.entity.User;
import com.memorator.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public RefreshToken create(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revokeAll(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
