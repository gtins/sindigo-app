package com.api.sindigo.core.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ⚠️ DEBUG ENDPOINT DESABILITADO
 * Este endpoint foi desabilitado por razões de segurança (informação disclosure).
 * Remova este arquivo em produção.
 */
/*
@RestController
@RequestMapping("/admin/audit/debug")
@RequiredArgsConstructor
@Slf4j
public class AuditDebugController {

    @GetMapping
    public ResponseEntity<?> debug() {
        // Endpoint desabilitado - expunha informações de segurança
        return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
    }
}
*/

