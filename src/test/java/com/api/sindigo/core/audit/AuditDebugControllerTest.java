package com.api.sindigo.core.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditDebugControllerTest {

    private final AuditDebugController controller = new AuditDebugController();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debugReturnsCurrentAuthenticationState() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        authentication.setDetails("mock-details");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var response = controller.debug();

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(Boolean.TRUE, body.get("authenticated"));
        assertEquals("admin@example.com", body.get("principal"));
        assertTrue(body.containsKey("authorities"));
        assertTrue(body.containsKey("details"));
    }
}

