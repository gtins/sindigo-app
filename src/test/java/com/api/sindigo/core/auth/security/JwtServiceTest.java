package com.api.sindigo.core.auth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Test
    void validateTokenReturnsValidResponseForWorkingToken() {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        when(provider.validateToken("token-ok")).thenReturn(true);
        when(provider.getExpirationTime()).thenReturn(3_600L);

        JwtService service = new JwtService(provider);
        JwtService.JwtValidationResponse response = service.validateToken("token-ok");

        assertTrue(response.isValid());
        assertNotNull(response.getExpiresAt());
    }

    @Test
    void validateTokenReturnsInvalidResponseWhenProviderRejectsToken() {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        when(provider.validateToken("bad-token")).thenReturn(false);

        JwtService service = new JwtService(provider);
        JwtService.JwtValidationResponse response = service.validateToken("bad-token");

        assertFalse(response.isValid());
    }
}

