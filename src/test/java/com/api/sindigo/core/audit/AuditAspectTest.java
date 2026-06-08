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

    @Test
    void auditOperationHandlesPutRequests() {
        UUID resourceId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/v1/tickets/" + resourceId);
        request.addHeader("User-Agent", "JUnit");
        request.addHeader("X-Forwarded-For", "192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, ResponseEntity.ok(Map.of("id", "123", "status", "updated")));

        var captor = forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("user@example.com", saved.getCreatedBy());
        assertEquals("UPDATE", saved.getAction());
        assertEquals("PUT", saved.getHttpMethod());
    }

    @Test
    void auditOperationHandlesDeleteRequests() {
        UUID resourceId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/tickets/" + resourceId);
        request.addHeader("User-Agent", "Chrome");
        request.addHeader("X-Forwarded-For", "172.16.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, ResponseEntity.ok(Map.of("message", "Deleted")));

        var captor = forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("DELETE", saved.getAction());
        assertEquals(200, saved.getHttpStatus());
    }

    @Test
    void auditOperationHandlesHeadRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest("HEAD", "/api/v1/tickets");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, Map.of());

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void auditOperationCapsturesiPAddressFromXForwardedFor() {
        UUID resourceId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/condominiums/" + resourceId);
        request.addHeader("X-Forwarded-For", "203.0.113.5, 198.51.100.12");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "token",
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, ResponseEntity.ok(Map.of("id", "456")));

        var captor = forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("203.0.113.5", saved.getIpAddress());
    }

    @Test
    void auditOperationHandlesResponseEntityWithErrorStatus() {
        UUID resourceId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/tickets/" + resourceId);
        request.addHeader("User-Agent", "Firefox");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "token",
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, ResponseEntity.badRequest().body(Map.of("error", "Invalid")));

        var captor = forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(400, saved.getHttpStatus());
    }

    @Test
    void auditOperationRemovesUserAgentIfNotPresent() {
        UUID resourceId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/activities/" + resourceId);
        // No User-Agent header added
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "token",
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, ResponseEntity.ok(Map.of()));

        var captor = forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertNotNull(saved);
    }

    @Test
    void auditOperationIdentifiesCondominiumResource() {
        UUID resourceId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/condominiums/" + resourceId);
        request.addHeader("User-Agent", "Test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "token",
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, ResponseEntity.ok(Map.of()));

        var captor = forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("Condominium", saved.getResource());
    }

    @Test
    void auditOperationIdentifiesActivityResource() {
        UUID resourceId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/v1/activities/" + resourceId);
        request.addHeader("User-Agent", "Test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "token",
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JoinPoint joinPoint = mock(JoinPoint.class);

        auditAspect.auditOperation(joinPoint, ResponseEntity.ok(Map.of()));

        var captor = forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("Activity", saved.getResource());
    }
}



