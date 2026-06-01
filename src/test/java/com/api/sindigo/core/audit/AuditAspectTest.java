package com.api.sindigo.core.audit;

import com.api.sindigo.core.audit.entities.AuditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuditAspectTest {

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditAspect auditAspect = new AuditAspect(auditLogRepository, objectMapper);

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditOperationPersistsLogForPostRequest() {
        UUID resourceId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/tickets/" + resourceId);
        request.addHeader("User-Agent", "JUnit");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, ResponseEntity.ok(Map.of("id", "123")));

        var captor = forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("admin@example.com", saved.getCreatedBy());
        assertEquals("CREATE", saved.getAction());
        assertEquals("Ticket", saved.getResource());
        assertEquals(resourceId, saved.getResourceId());
        assertEquals("POST", saved.getHttpMethod());
        assertEquals(200, saved.getHttpStatus());
        assertEquals("10.0.0.1", saved.getIpAddress());
        assertEquals("JUnit", saved.getUserAgent());
        assertNotNull(saved.getNewValue());
        assertTrue(saved.getNewValue().contains("admin@example.com"));
        assertTrue(saved.getNewValue().contains("123"));
    }

    @Test
    void auditOperationSkipsGetRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/tickets");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, Map.of("id", "123"));

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }
}



