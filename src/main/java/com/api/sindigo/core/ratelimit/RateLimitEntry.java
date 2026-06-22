package com.api.sindigo.core.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimitEntry {
    private String endpoint;
    private String clientIdentifier;
    private long timestamp;
}

