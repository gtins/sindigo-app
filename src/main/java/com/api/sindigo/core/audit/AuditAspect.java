package com.api.sindigo.core.audit;

import com.api.sindigo.core.audit.entities.AuditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private static final String UNKNOWN = "UNKNOWN";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @AfterReturning(
            value = """
                    execution(* com.api.sindigo.core.condominium.*Controller.*(..)) ||
                    execution(* com.api.sindigo.core.ticket.*Controller.*(..)) ||
                    execution(* com.api.sindigo.core.user.UserController.*(..))
                    """,
            returning = "result"
    )
    @SuppressWarnings("unused")
    public void auditOperation(JoinPoint joinPoint, Object result) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;

            HttpServletRequest request = attributes.getRequest();
            String method = request.getMethod();
            
            if ("GET".equalsIgnoreCase(method)) return;

            String uri = request.getRequestURI();
            String ipAddress = getClientIp(request);

            // Usuário e role
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = "SYSTEM";
            String userRole = "SYSTEM";
            
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                userEmail = auth.getName();
                userRole = auth.getAuthorities().stream()
                        .map(a -> {
                            String authority = a.getAuthority();
                            return authority != null ? authority.replace("ROLE_", "") : UNKNOWN;
                        })
                        .findFirst()
                        .orElse(UNKNOWN);
            }

            String action = getAction(method);
            String resource = getResource(uri);
            UUID resourceId = extractResourceId(uri);

            // Dados do resultado
            String dadosJson = extractData(result);

            // JSON com informações completas
            String auditJson = buildAuditJson(userEmail, userRole, action, dadosJson);

            AuditLog auditLog = AuditLog.builder()
                    .createdBy(userEmail)
                    .action(action)
                    .resource(resource)
                    .resourceId(resourceId)
                    .newValue(auditJson)
                    .ipAddress(ipAddress)
                    .userAgent(request.getHeader("User-Agent"))
                    .httpMethod(method)
                    .httpStatus(200)
                    .createdAt(LocalDateTime.now())
                    .details(uri)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("AUDITADO: {} {} - {} [{}]", method, uri, userEmail, userRole);

        } catch (Exception e) {
            log.error("Erro auditoria", e);
        }
    }

    private String buildAuditJson(String userEmail, String userRole, String action, String dados) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("Quem", userEmail);
            node.put("Role", userRole);
            node.put("Acao", action);
            node.put("Quando", LocalDateTime.now().toString());
            
            if (dados != null && !dados.equals("{}")) {
                addDadosToNode(node, dados);
            }
            
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void addDadosToNode(ObjectNode node, String dados) {
        try {
            node.set("dados", objectMapper.readTree(dados));
        } catch (Exception e) {
            node.put("dados", dados);
        }
    }

    private String extractData(Object result) {
        try {
            Object data = result;
            
            // Se é ResponseEntity, pega o body
            data = extractResponseEntityBody(result, data);

            if (data != null) {
                return objectMapper.writeValueAsString(data);
            }
        } catch (Exception e) {
            log.debug("Erro extrair dados");
        }
        return "{}";
    }

    private Object extractResponseEntityBody(Object result, Object data) {
        try {
            Field bodyField = result.getClass().getDeclaredField("body");
            bodyField.setAccessible(true);
            return bodyField.get(result);
        } catch (NoSuchFieldException e) {
            // Não é ResponseEntity, retorna data como está
            return data;
        } catch (IllegalAccessException e) {
            log.trace("Acesso negado ao campo body: {}", e.getMessage());
            return data;
        }
    }

    private String getAction(String httpMethod) {
        return switch (httpMethod.toUpperCase()) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> UNKNOWN;
        };
    }

    private String getResource(String uri) {
        if (uri.contains("condominium") || uri.contains("condominiums")) return "Condominium";
        if (uri.contains("ticket")) return "Ticket";
        if (uri.contains("change-role")) return "UserRole";
        if (uri.contains("/user/")) return "User";
        if (uri.contains("/auth/")) return "Auth";
        return "Unknown";
    }

    private UUID extractResourceId(String uri) {
        String[] parts = uri.split("/");
        for (String part : parts) {
            try {
                return UUID.fromString(part);
            } catch (IllegalArgumentException e) {
                // Parte não é um UUID válido, continua iterando
                log.trace("Parte não é UUID: {}", part);
            }
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}