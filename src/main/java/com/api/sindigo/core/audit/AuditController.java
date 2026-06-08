package com.api.sindigo.core.audit;

import com.api.sindigo.core.audit.entities.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private static final String ERROR_RESPONSE_KEY = "error";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ONLY_ADMIN_CAN_ACCESS_AUDIT = "Apenas ADMIN pode acessar auditoria";
    private static final String UNEXPECTED_ERROR_MESSAGE = "Erro inesperado ao buscar logs de auditoria";

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<Object> getAllAuditLogs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return getAuditLogs(
                authentication,
                page,
                size,
                auditLogRepository::findAll,
                "Erro ao buscar logs de auditoria"
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getAuditLogsByUser(
            Authentication authentication,
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return getAuditLogs(
                authentication,
                page,
                size,
                pageable -> auditLogRepository.findByCreatedBy(userId, pageable),
                "Erro ao buscar logs por usuário"
        );
    }

    @GetMapping("/resource/{resource}")
    public ResponseEntity<Object> getAuditLogsByResource(
            Authentication authentication,
            @PathVariable String resource,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return getAuditLogs(
                authentication,
                page,
                size,
                pageable -> auditLogRepository.findByResource(resource, pageable),
                "Erro ao buscar logs por recurso"
        );
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<Object> getAuditLogsByAction(
            Authentication authentication,
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return getAuditLogs(
                authentication,
                page,
                size,
                pageable -> auditLogRepository.findByAction(action, pageable),
                "Erro ao buscar logs por ação"
        );
    }

    private ResponseEntity<Object> getAuditLogs(
            Authentication authentication,
            int page,
            int size,
            AuditLogPageFetcher fetcher,
            String errorLogMessage) {

        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(buildErrorBody(ONLY_ADMIN_CAN_ACCESS_AUDIT));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLog> logs = fetcher.fetch(pageable);

            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error(errorLogMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorBody(e.getMessage()));
        }
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }

    private Map<String, String> buildErrorBody(String message) {
        return Map.of(ERROR_RESPONSE_KEY, message != null ? message : UNEXPECTED_ERROR_MESSAGE);
    }

    @FunctionalInterface
    private interface AuditLogPageFetcher {
        Page<AuditLog> fetch(Pageable pageable);
    }
}