package com.api.sindigo.core.ratelimit;

import java.lang.annotation.*;

/**
 * Anotação para aplicar rate limiting a métodos controller
 * 
 * Uso: @RateLimited(maxRequests = 5, windowSeconds = 60)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    
    /**
     * Número máximo de requisições permitidas na janela de tempo
     */
    int maxRequests() default 10;
    
    /**
     * Janela de tempo em segundos
     */
    int windowSeconds() default 60;
    
    /**
     * Mensagem de erro personalizada
     */
    String message() default "Too many requests. Please try again later.";
}

