package com.api.sindigo.core.audit;

import com.api.sindigo.core.audit.entities.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditControllerTest {

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final AuditController auditController = new AuditController(auditLogRepository);

    @Test
    void adminCanQueryAuditLogsUsingAllEndpoints() {
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        Page<AuditLog> page = new PageImpl<>(List.of(buildLog()));
        when(auditLogRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);
        when(auditLogRepository.findByCreatedBy("user@example.com", PageRequest.of(1, 10))).thenReturn(page);
        when(auditLogRepository.findByResource("Ticket", PageRequest.of(2, 5))).thenReturn(page);
        when(auditLogRepository.findByAction("CREATE", PageRequest.of(3, 15))).thenReturn(page);

        var allResult = auditController.getAllAuditLogs(adminAuth, 0, 20);
        var userResult = auditController.getAuditLogsByUser(adminAuth, "user@example.com", 1, 10);
        var resourceResult = auditController.getAuditLogsByResource(adminAuth, "Ticket", 2, 5);
        var actionResult = auditController.getAuditLogsByAction(adminAuth, "CREATE", 3, 15);

        assertEquals(200, allResult.getStatusCode().value());
        assertEquals(200, userResult.getStatusCode().value());
        assertEquals(200, resourceResult.getStatusCode().value());
        assertEquals(200, actionResult.getStatusCode().value());
        assertInstanceOf(Page.class, allResult.getBody());
        assertInstanceOf(Page.class, userResult.getBody());
        assertInstanceOf(Page.class, resourceResult.getBody());
        assertInstanceOf(Page.class, actionResult.getBody());
        verify(auditLogRepository).findAll(PageRequest.of(0, 20));
        verify(auditLogRepository).findByCreatedBy("user@example.com", PageRequest.of(1, 10));
        verify(auditLogRepository).findByResource("Ticket", PageRequest.of(2, 5));
        verify(auditLogRepository).findByAction("CREATE", PageRequest.of(3, 15));
    }

    @Test
    void nonAdminIsForbiddenFromAuditEndpoints() {
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        var result = auditController.getAllAuditLogs(userAuth, 0, 20);

        assertEquals(403, result.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) result.getBody();
        assertTrue(body != null && body.containsValue("Apenas ADMIN pode acessar auditoria"));
    }

    private AuditLog buildLog() {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .createdBy("admin@example.com")
                .action("CREATE")
                .resource("Ticket")
                .resourceId(UUID.randomUUID())
                .newValue("{}")
                .httpMethod("POST")
                .httpStatus(200)
                .createdAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .details("/api/v1/tickets")
                .build();
    }
}

