package com.api.sindigo.core.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitAspectTest {

    private final RateLimitService rateLimitService = mock(RateLimitService.class);
    private final RateLimitAspect aspect = new RateLimitAspect(rateLimitService);

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void enforceRateLimitAllowsExecutionWhenThereIsNoHttpRequest() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        RateLimited rateLimited = mockRateLimited(5, 60, "Too many requests");
        Object expectedResponse = "ok";

        when(joinPoint.proceed()).thenReturn(expectedResponse);

        Object result = aspect.enforceRateLimit(joinPoint, rateLimited);

        assertEquals(expectedResponse, result);
        verify(joinPoint).proceed();
        verifyNoInteractions(rateLimitService);
    }

    @Test
    void enforceRateLimitAllowsExecutionWhenRequestIsAllowedByIp() throws Throwable {
        MockHttpServletRequest request = request("POST", "/auth/login");
        request.setRemoteAddr("127.0.0.1");
        setRequest(request);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        RateLimited rateLimited = mockRateLimited(5, 60, "Too many requests");
        Object expectedResponse = "allowed";

        when(rateLimitService.allowRequest("POST /auth/login", "ip:127.0.0.1", 5, 60))
                .thenReturn(true);
        when(joinPoint.proceed()).thenReturn(expectedResponse);

        Object result = aspect.enforceRateLimit(joinPoint, rateLimited);

        assertEquals(expectedResponse, result);
        verify(rateLimitService).allowRequest("POST /auth/login", "ip:127.0.0.1", 5, 60);
        verify(joinPoint).proceed();
    }

    @Test
    void enforceRateLimitUsesAuthenticatedUserAsClientIdentifier() throws Throwable {
        MockHttpServletRequest request = request("POST", "/auth/login");
        setRequest(request);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-123", "token", List.of())
        );

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        RateLimited rateLimited = mockRateLimited(3, 30, "Too many login attempts");

        when(rateLimitService.allowRequest("POST /auth/login", "user:user-123", 3, 30))
                .thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.enforceRateLimit(joinPoint, rateLimited);

        assertEquals("ok", result);
        verify(rateLimitService).allowRequest("POST /auth/login", "user:user-123", 3, 30);
        verify(joinPoint).proceed();
    }

    @Test
    void enforceRateLimitUsesXForwardedForWhenUserIsAnonymous() throws Throwable {
        MockHttpServletRequest request = request("POST", "/auth/register");
        request.addHeader("X-Forwarded-For", "10.0.0.5");
        setRequest(request);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", "token", List.of())
        );

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        RateLimited rateLimited = mockRateLimited(2, 60, "Too many registration attempts");

        when(rateLimitService.allowRequest("POST /auth/register", "ip:10.0.0.5", 2, 60))
                .thenReturn(true);
        when(joinPoint.proceed()).thenReturn("created");

        Object result = aspect.enforceRateLimit(joinPoint, rateLimited);

        assertEquals("created", result);
        verify(rateLimitService).allowRequest("POST /auth/register", "ip:10.0.0.5", 2, 60);
        verify(joinPoint).proceed();
    }

    @Test
    void enforceRateLimitReturnsTooManyRequestsWhenLimitIsExceeded() throws Throwable {
        MockHttpServletRequest request = request("POST", "/auth/login");
        request.setRemoteAddr("127.0.0.1");
        setRequest(request);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        RateLimited rateLimited = mockRateLimited(1, 60, "Too many login attempts");

        when(rateLimitService.allowRequest("POST /auth/login", "ip:127.0.0.1", 1, 60))
                .thenReturn(false);
        when(rateLimitService.getRemainingRequests("POST /auth/login", "ip:127.0.0.1", 1, 60))
                .thenReturn(0);

        Object result = aspect.enforceRateLimit(joinPoint, rateLimited);

        ResponseEntity<?> response = assertInstanceOf(ResponseEntity.class, result);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("Too many login attempts", body.get("error"));
        assertEquals(0, body.get("remaining_requests"));

        verify(joinPoint, never()).proceed();
        verify(rateLimitService).getRemainingRequests("POST /auth/login", "ip:127.0.0.1", 1, 60);
    }

    private RateLimited mockRateLimited(int maxRequests, int windowSeconds, String message) {
        RateLimited rateLimited = mock(RateLimited.class);

        when(rateLimited.maxRequests()).thenReturn(maxRequests);
        when(rateLimited.windowSeconds()).thenReturn(windowSeconds);
        when(rateLimited.message()).thenReturn(message);

        return rateLimited;
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);
        return request;
    }

    private void setRequest(HttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}