package com.api.sindigo.core.ratelimit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {

    int maxRequests() default 10;

    int windowSeconds() default 60;

    String message() default "Too many requests. Please try again later.";
}

