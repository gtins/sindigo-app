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

@RestController
@RequestMapping("/admin/audit/debug")
@RequiredArgsConstructor
@Slf4j
public class AuditDebugController {

    @GetMapping
    public ResponseEntity<?> debug() {
        Map<String, Object> response = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        response.put("authenticated", auth != null && auth.isAuthenticated());
        response.put("principal", auth != null ? auth.getPrincipal() : null);
        response.put("authorities", auth != null ? 
            auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList()) 
            : null);
        response.put("details", auth != null ? auth.getDetails() : null);
        
        return ResponseEntity.ok(response);
    }
}

