package com.api.sindigo.core.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RateLimitEntryTest {

    @Test
    void rateLimitEntryStoresConstructorValues() {
        RateLimitEntry entry = new RateLimitEntry(
                "POST /auth/login",
                "ip:127.0.0.1",
                123456L
        );

        assertEquals("POST /auth/login", entry.getEndpoint());
        assertEquals("ip:127.0.0.1", entry.getClientIdentifier());
        assertEquals(123456L, entry.getTimestamp());
    }

    @Test
    void rateLimitEntryAllowsUpdatingValues() {
        RateLimitEntry entry = new RateLimitEntry(
                "POST /auth/login",
                "ip:127.0.0.1",
                123456L
        );

        entry.setEndpoint("POST /auth/register");
        entry.setClientIdentifier("user:abc");
        entry.setTimestamp(789L);

        assertEquals("POST /auth/register", entry.getEndpoint());
        assertEquals("user:abc", entry.getClientIdentifier());
        assertEquals(789L, entry.getTimestamp());
    }

    @Test
    void rateLimitEntryEqualsUsesAllFields() {
        RateLimitEntry first = new RateLimitEntry(
                "POST /auth/login",
                "ip:127.0.0.1",
                123456L
        );

        RateLimitEntry second = new RateLimitEntry(
                "POST /auth/login",
                "ip:127.0.0.1",
                123456L
        );

        RateLimitEntry different = new RateLimitEntry(
                "POST /auth/login",
                "ip:127.0.0.2",
                123456L
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
    }
}