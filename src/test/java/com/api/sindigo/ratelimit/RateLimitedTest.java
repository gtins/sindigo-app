package com.api.sindigo.core.ratelimit;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RateLimitedTest {

    @Test
    void rateLimitedAnnotationUsesDefaultValues() throws Exception {
        Method method = TestController.class.getDeclaredMethod("defaultLimitedEndpoint");

        RateLimited annotation = method.getAnnotation(RateLimited.class);

        assertNotNull(annotation);
        assertEquals(10, annotation.maxRequests());
        assertEquals(60, annotation.windowSeconds());
        assertEquals("Too many requests. Please try again later.", annotation.message());
    }

    @Test
    void rateLimitedAnnotationUsesCustomValues() throws Exception {
        Method method = TestController.class.getDeclaredMethod("customLimitedEndpoint");

        RateLimited annotation = method.getAnnotation(RateLimited.class);

        assertNotNull(annotation);
        assertEquals(3, annotation.maxRequests());
        assertEquals(120, annotation.windowSeconds());
        assertEquals("Custom limit message", annotation.message());
    }

    private static class TestController {

        @RateLimited
        void defaultLimitedEndpoint() {
            // Used only for annotation reflection test.
        }

        @RateLimited(maxRequests = 3, windowSeconds = 120, message = "Custom limit message")
        void customLimitedEndpoint() {
            // Used only for annotation reflection test.
        }
    }
}