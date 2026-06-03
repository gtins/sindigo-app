package com.api.sindigo.core.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspect AOP que implementa rate limiting em métodos anotados com @RateLimited
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {
    
    private final RateLimitService rateLimitService;
    
    @Around("@annotation(rateLimited)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        HttpServletRequest request = getHttpRequest();
        
        if (request == null) {
            // Se não for uma requisição HTTP, permitir
            return joinPoint.proceed();
        }
        
        String endpoint = request.getMethod() + " " + request.getRequestURI();
        String clientIdentifier = getClientIdentifier(request);
        
        boolean allowed = rateLimitService.allowRequest(
            endpoint,
            clientIdentifier,
            rateLimited.maxRequests(),
            rateLimited.windowSeconds()
        );
        
        if (!allowed) {
            int remaining = rateLimitService.getRemainingRequests(
                endpoint,
                clientIdentifier,
                rateLimited.maxRequests(),
                rateLimited.windowSeconds()
            );
            
            log.warn("Rate limit exceeded for {}: {}", clientIdentifier, endpoint);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", rateLimited.message());
            errorResponse.put("remaining_requests", remaining);
            
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(errorResponse);
        }
        
        return joinPoint.proceed();
    }
    
    /**
     * Obtém o identificador único do cliente
     * Prioridade: UserId autenticado > IP do cliente
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // Tentar obter do usuário autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()))) {
            return "user:" + auth.getPrincipal();
        }
        
        // Fallback para IP do cliente
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        
        return "ip:" + clientIp;
    }
    
    /**
     * Obtém a requisição HTTP do contexto
     */
    private HttpServletRequest getHttpRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

