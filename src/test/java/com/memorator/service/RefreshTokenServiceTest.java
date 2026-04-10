package com.memorator.service;

import com.memorator.entity.RefreshToken;
import com.memorator.entity.User;
import com.memorator.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static final long REFRESH_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", REFRESH_EXPIRATION_MS);
    }

    private User buildUser() {
        return User.builder().id(1L).email("test@example.com").login("testuser").build();
    }

    @Test
    void create_success() {
        User user = buildUser();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.create(user);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getToken()).isNotNull();
        assertThat(result.isRevoked()).isFalse();
        assertThat(result.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void validate_success() {
        User user = buildUser();
        RefreshToken token = RefreshToken.builder()
                .token("valid-token")
                .user(user)
                .revoked(false)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-token")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.validate("valid-token");

        assertThat(result).isEqualTo(token);
    }

    @Test
    void validate_tokenNotFound() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validate("bad-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void validate_tokenExpired() {
        User user = buildUser();
        RefreshToken token = RefreshToken.builder()
                .token("expired-token")
                .user(user)
                .revoked(false)
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validate("expired-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refresh token expired");
    }

    @Test
    void revokeAll_callsDeleteByUser() {
        User user = buildUser();

        refreshTokenService.revokeAll(user);

        verify(refreshTokenRepository).deleteByUser(user);
    }
}
