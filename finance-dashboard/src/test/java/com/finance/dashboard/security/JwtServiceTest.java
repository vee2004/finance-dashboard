package com.finance.dashboard.security;

import com.finance.dashboard.entity.Role;
import com.finance.dashboard.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    // Same 256-bit hex key used in test application.yml
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86400000L); // 24h

        testUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@finance.com")
                .password("encoded")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("generateToken: produces a non-blank token")
    void generateToken_returnsNonBlankToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("extractUsername: retrieves correct username from token")
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("isTokenValid: valid token with matching user returns true")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: token for different user returns false")
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateToken(testUser);

        User anotherUser = User.builder()
                .id(2L).username("viewer").email("v@v.com")
                .password("x").role(Role.VIEWER).build();

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid: expired token returns false")
    void isTokenValid_expiredToken_returnsFalse() {
        // Override expiration to 1ms so token expires immediately
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 1L);
        String token = jwtService.generateToken(testUser);

        // Wait for expiration
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertThat(jwtService.isTokenValid(token, testUser)).isFalse();
    }
}
