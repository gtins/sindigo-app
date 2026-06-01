package com.api.sindigo.core.auth.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityContextHelperTest {

    private final SecurityContextHelper helper = new SecurityContextHelper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthenticatedUserIdReadsUuidFromPrincipal() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), "token", List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        assertEquals(userId, helper.getAuthenticatedUserId());
    }

    @Test
    void getAuthenticatedUserEmailReadsDetailsFromAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user-id", "token", List.of());
        ((UsernamePasswordAuthenticationToken) authentication).setDetails("user@example.com");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        assertEquals("user@example.com", helper.getAuthenticatedUserEmail());
    }

    @Test
    void getAuthenticatedUserIdRejectsAnonymousUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        assertThrows(IllegalStateException.class, () -> helper.getAuthenticatedUserId());
    }
}


