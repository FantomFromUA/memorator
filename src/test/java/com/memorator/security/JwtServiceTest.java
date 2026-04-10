package com.memorator.security;

import com.memorator.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "this-is-a-test-secret-that-is-long-enough-for-hmac";
    private static final long EXPIRATION_MS = 15 * 60 * 1000L; // 15 min

    private JwtService jwtService;

    private User buildUser() {
        return User.builder()
                .id(42L)
                .email("jwt@example.com")
                .login("jwtuser")
                .build();
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);
    }

    @Test
    void generateToken_subjectIsUserId() {
        User user = buildUser();
        String token = jwtService.generateToken(user);

        Claims claims = jwtService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("42");
    }

    @Test
    void generateToken_containsEmailClaim() {
        User user = buildUser();
        String token = jwtService.generateToken(user);

        Claims claims = jwtService.parseToken(token);
        assertThat(claims.get("email", String.class)).isEqualTo("jwt@example.com");
    }

    @Test
    void generateToken_containsLoginClaim() {
        User user = buildUser();
        String token = jwtService.generateToken(user);

        Claims claims = jwtService.parseToken(token);
        assertThat(claims.get("login", String.class)).isEqualTo("jwtuser");
    }

    @Test
    void extractUserId_returnsCorrectId() {
        User user = buildUser();
        String token = jwtService.generateToken(user);

        Long extractedId = jwtService.extractUserId(token);
        assertThat(extractedId).isEqualTo(42L);
    }

    @Test
    void parseToken_expiredToken() {
        User user = buildUser();
        ReflectionTestUtils.setField(jwtService, "expiration", 0L);
        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void parseToken_invalidToken() {
        assertThatThrownBy(() -> jwtService.parseToken("not.a.valid.jwt"))
                .isInstanceOf(JwtException.class);
    }
}
