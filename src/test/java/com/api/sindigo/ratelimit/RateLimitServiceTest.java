package com.api.sindigo.core.ratelimit;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private final RateLimitService rateLimitService = new RateLimitService();

    @Test
    void allowRequestReturnsTrueWhenLimitHasNotBeenReached() {
        boolean firstRequest = rateLimitService.allowRequest(
                "POST /auth/login",
                "ip:127.0.0.1",
                2,
                60
        );

        boolean secondRequest = rateLimitService.allowRequest(
                "POST /auth/login",
                "ip:127.0.0.1",
                2,
                60
        );

        assertTrue(firstRequest);
        assertTrue(secondRequest);
        assertEquals(0, rateLimitService.getRemainingRequests(
                "POST /auth/login",
                "ip:127.0.0.1",
                2,
                60
        ));
    }

    @Test
    void allowRequestReturnsFalseWhenLimitHasBeenReached() {
        String endpoint = "POST /auth/login";
        String clientIdentifier = "ip:127.0.0.1";

        assertTrue(rateLimitService.allowRequest(endpoint, clientIdentifier, 1, 60));

        boolean result = rateLimitService.allowRequest(endpoint, clientIdentifier, 1, 60);

        assertFalse(result);
        assertEquals(0, rateLimitService.getRemainingRequests(endpoint, clientIdentifier, 1, 60));
    }

    @Test
    void getRemainingRequestsReturnsMaxRequestsWhenThereIsNoRequestLog() {
        int remaining = rateLimitService.getRemainingRequests(
                "POST /auth/register",
                "ip:10.0.0.1",
                5,
                60
        );

        assertEquals(5, remaining);
        assertTrue(remaining > 0);
        assertNotEquals(0, remaining);
    }

    @Test
    void getRemainingRequestsIgnoresRequestsOutsideWindow() throws Exception {
        String endpoint = "POST /auth/login";
        String clientIdentifier = "ip:127.0.0.1";
        String key = endpoint + ":" + clientIdentifier;

        long oldTimestamp = System.currentTimeMillis() - 120_000L;

        Map<String, List<Long>> requestLog = getRequestLog();
        requestLog.put(key, new ArrayList<>(List.of(oldTimestamp)));

        int remaining = rateLimitService.getRemainingRequests(endpoint, clientIdentifier, 3, 60);

        assertEquals(3, remaining);
        assertTrue(requestLog.containsKey(key));
        assertEquals(1, requestLog.get(key).size());
    }

    @Test
    void allowRequestRemovesExpiredRequestsBeforeCheckingLimit() throws Exception {
        String endpoint = "POST /auth/login";
        String clientIdentifier = "ip:127.0.0.1";
        String key = endpoint + ":" + clientIdentifier;

        long oldTimestamp = System.currentTimeMillis() - 120_000L;

        Map<String, List<Long>> requestLog = getRequestLog();
        requestLog.put(key, new ArrayList<>(List.of(oldTimestamp)));

        boolean result = rateLimitService.allowRequest(endpoint, clientIdentifier, 1, 60);

        assertTrue(result);
        assertEquals(1, requestLog.get(key).size());
        assertTrue(requestLog.get(key).get(0) > oldTimestamp);
    }

    @Test
    void cleanupOldEntriesRemovesEmptyOldEntries() throws Exception {
        String key = "POST /auth/login:ip:127.0.0.1";
        long oldTimestamp = System.currentTimeMillis() - 7_200_000L;

        Map<String, List<Long>> requestLog = getRequestLog();
        requestLog.put(key, new ArrayList<>(List.of(oldTimestamp)));

        rateLimitService.cleanupOldEntries();

        assertFalse(requestLog.containsKey(key));
        assertTrue(requestLog.isEmpty());
        assertEquals(0, requestLog.size());
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Long>> getRequestLog() throws Exception {
        Field field = RateLimitService.class.getDeclaredField("requestLog");
        field.setAccessible(true);
        return (Map<String, List<Long>>) field.get(rateLimitService);
    }
}