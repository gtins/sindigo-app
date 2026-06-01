package com.api.sindigo.config;

import com.api.sindigo.core.auth.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig(mock(JwtAuthenticationFilter.class));

    @Test
    void passwordEncoderReturnsBcryptEncoder() {
        assertInstanceOf(BCryptPasswordEncoder.class, securityConfig.passwordEncoder());
    }

    @Test
    void corsConfigurationSourceExposesExpectedOriginsAndMethods() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(configuration);
        assertNotNull(configuration.getAllowedOrigins());
        assertNotNull(configuration.getAllowedMethods());
        assertTrue(configuration.getAllowedOrigins().containsAll(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:4200",
                "http://localhost:8080"
        )));
        assertTrue(configuration.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")));
    }
}


