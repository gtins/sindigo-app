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
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private static final String UNKNOWN = "UNKNOWN";
    private static final String EMPTY_JSON = "{}";

    private static final String SYSTEM = "SYSTEM";
    private static final String ANONYMOUS_USER = "anonymousUser";
    private static final String ROLE_PREFIX = "ROLE_";

    private static final String GET = "GET";
    private static final String HEAD = "HEAD";

    private static final String CREATE = "CREATE";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";

    private static final String CONDOMINIUM_RESOURCE = "Condominium";
    private static final String TICKET_RESOURCE = "Ticket";
    private static final String ACTIVITY_RESOURCE = "Activity";
    private static final String USER_ROLE_RESOURCE = "UserRole";
    private static final String USER_RESOURCE = "User";
    private static final String AUTH_RESOURCE = "Auth";
    private static final String UNKNOWN_RESOURCE = "Unknown";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @AfterReturning(
            value = """
                    execution(* com.api.sindigo.core.condominium.*Controller.*(..)) ||
                    execution(* com.api.sindigo.core.ticket.*Controller.*(..)) ||
                    execution(* com.api.sindigo.core.user.UserController.*(..)) ||
                    execution(* com.api.sindigo.core.activity.*Controller.*(..)) ||
                    execution(* com.api.sindigo.core.activityinstance.*Controller.*(..))
                    """,
            returning = "result"
    )
    @SuppressWarnings("unused")
    public void auditOperation(JoinPoint joinPoint, Object result) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String method = request.getMethod();

            if (GET.equalsIgnoreCase(method) || HEAD.equalsIgnoreCase(method)) {
                return;
            }

            String uri = request.getRequestURI();
            String ipAddress = getClientIp(request);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = SYSTEM;
            String userRole = SYSTEM;

            if (auth != null && auth.isAuthenticated() && !ANONYMOUS_USER.equals(auth.getPrincipal())) {
                userEmail = auth.getName();
                userRole = auth.getAuthorities()
                        .stream()
                        .map(authority -> {
                            String authorityName = authority.getAuthority();
                            return authorityName != null ? authorityName.replace(ROLE_PREFIX, "") : UNKNOWN;
                        })
                        .findFirst()
                        .orElse(UNKNOWN);
            }

            String action = getAction(method);
            String resource = getResource(uri);
            UUID resourceId = extractResourceId(uri);
            int httpStatus = extractHttpStatus(result);
            String dadosJson = extractData(result);
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
                    .httpStatus(httpStatus)
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

            if (dados != null && !EMPTY_JSON.equals(dados)) {
                addDadosToNode(node, dados);
            }

            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return EMPTY_JSON;
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
            Object data = extractResponseBody(result);

            if (data != null) {
                return objectMapper.writeValueAsString(data);
            }
        } catch (Exception e) {
            log.debug("Erro extrair dados", e);
        }

        return EMPTY_JSON;
    }

    private Object extractResponseBody(Object result) {
        if (result == null) {
            return null;
        }

        try {
            Method getBodyMethod = result.getClass().getMethod("getBody");
            return getBodyMethod.invoke(result);
        } catch (NoSuchMethodException e) {
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.trace("Não foi possível extrair body da resposta: {}", e.getMessage());
            return result;
        }
    }

    private String getAction(String httpMethod) {
        return switch (httpMethod.toUpperCase()) {
            case "POST" -> CREATE;
            case "PUT", "PATCH" -> UPDATE;
            case "DELETE" -> DELETE;
            default -> UNKNOWN;
        };
    }

    private String getResource(String uri) {
        String normalizedUri = uri.toLowerCase();

        if (normalizedUri.contains("change-role")) {
            return USER_ROLE_RESOURCE;
        }

        if (normalizedUri.contains("ticket") || normalizedUri.contains("tickets")) {
            return TICKET_RESOURCE;
        }

        if (normalizedUri.contains("activity")
                || normalizedUri.contains("activities")
                || normalizedUri.contains("activity-instance")
                || normalizedUri.contains("activity-instances")
                || normalizedUri.contains("activityinstance")) {
            return ACTIVITY_RESOURCE;
        }

        if (normalizedUri.contains("/user/") || normalizedUri.endsWith("/user")) {
            return USER_RESOURCE;
        }

        if (normalizedUri.contains("/auth/") || normalizedUri.endsWith("/auth")) {
            return AUTH_RESOURCE;
        }

        if (normalizedUri.contains("condominium") || normalizedUri.contains("condominiums")) {
            return CONDOMINIUM_RESOURCE;
        }

        return UNKNOWN_RESOURCE;
    }

    private int extractHttpStatus(Object result) {
        if (result == null) {
            return HttpStatusCode.valueOf(200).value();
        }

        try {
            Method getStatusCodeMethod = result.getClass().getMethod("getStatusCode");
            Object statusCode = getStatusCodeMethod.invoke(result);

            if (statusCode instanceof HttpStatusCode httpStatusCode) {
                return httpStatusCode.value();
            }

            if (statusCode != null) {
                return extractStatusFromText(statusCode.toString());
            }
        } catch (NoSuchMethodException e) {
            log.trace("Resultado não possui getStatusCode: {}", result.getClass().getName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.trace("Could not extract HTTP status: {}", e.getMessage());
        }

        return 200;
    }

    private int extractStatusFromText(String statusText) {
        if (statusText.contains("400")) {
            return 400;
        }

        if (statusText.contains("401")) {
            return 401;
        }

        if (statusText.contains("403")) {
            return 403;
        }

        if (statusText.contains("404")) {
            return 404;
        }

        if (statusText.contains("500")) {
            return 500;
        }

        return 200;
    }

    private UUID extractResourceId(String uri) {
        String[] parts = uri.split("/");

        for (String part : parts) {
            try {
                return UUID.fromString(part);
            } catch (IllegalArgumentException e) {
                log.trace("Parte não é UUID: {}", part);
            }
        }

        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}