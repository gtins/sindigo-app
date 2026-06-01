package com.api.sindigo.core.auth.security;

import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.core.user.entities.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(provider, "jwtSecret", "1234567890123456789012345678901212345678901234567890123456789012");
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 3_600_000L);
    }

    @Test
    void generateTokenAndReadBackClaims() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("maria@example.com")
                .role(UserRole.ADMIN)
                .build();

        String token = provider.generateToken(user);

        assertTrue(provider.validateToken(token));
        assertEquals(userId.toString(), provider.getUserIdFromToken(token));
        assertEquals("maria@example.com", provider.getEmailFromToken(token));
        assertEquals("ADMIN", provider.getRoleFromToken(token));
        assertTrue(provider.getExpirationTime() > 0);
    }

    @Test
    void validateTokenRejectsMalformedToken() {
        assertFalse(provider.validateToken("not-a-token"));
    }
}


