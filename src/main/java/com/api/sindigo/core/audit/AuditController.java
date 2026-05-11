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

    private final AuditLogRepository auditLogRepository;

    /**
     * GET /admin/audit?page=0&size=20
     * Retorna todos os logs de auditoria paginados
     */
    @GetMapping
    public ResponseEntity<?> getAllAuditLogs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            // Validar se é ADMIN
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Apenas ADMIN pode acessar auditoria"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLog> logs = auditLogRepository.findAll(pageable);
            
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs de auditoria", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /admin/audit/user/{userId}
     * Retorna logs filtrados por usuário
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAuditLogsByUser(
            Authentication authentication,
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Apenas ADMIN pode acessar auditoria"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLog> logs = auditLogRepository.findByCreatedBy(userId, pageable);
            
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por usuário", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /admin/audit/resource/{resource}
     * Retorna logs filtrados por recurso
     */
    @GetMapping("/resource/{resource}")
    public ResponseEntity<?> getAuditLogsByResource(
            Authentication authentication,
            @PathVariable String resource,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Apenas ADMIN pode acessar auditoria"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLog> logs = auditLogRepository.findByResource(resource, pageable);
            
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por recurso", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /admin/audit/action/{action}
     * Retorna logs filtrados por ação
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<?> getAuditLogsByAction(
            Authentication authentication,
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Apenas ADMIN pode acessar auditoria"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLog> logs = auditLogRepository.findByAction(action, pageable);
            
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs por ação", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Valida se o usuário é ADMIN
     */
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}

