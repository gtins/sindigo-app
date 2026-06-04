package com.api.sindigo.core.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço de Rate Limiting em memória
 * 
 * Manter controle de requisições por cliente e endpoint
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {
    
    // Estrutura: {endpoint}:{clientId} -> lista de timestamps
    private final Map<String, List<Long>> requestLog = new ConcurrentHashMap<>();
    
    /**
     * Verifica se uma requisição é permitida
     * 
     * @param endpoint endpoint (ex: GET /api/users)
     * @param clientIdentifier identificador do cliente (user:uuid ou ip:xxx.xxx.xxx.xxx)
     * @param maxRequests número máximo de requisições
     * @param windowSeconds janela de tempo em segundos
     * @return true se permitido, false se excedeu limite
     */
    public boolean allowRequest(String endpoint, String clientIdentifier, int maxRequests, int windowSeconds) {
        String key = endpoint + ":" + clientIdentifier;
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);
        
        List<Long> requests = requestLog.computeIfAbsent(key, k -> new ArrayList<>());
        
        // Remover requisições fora da janela
        requests.removeIf(timestamp -> timestamp < windowStart);
        
        // Verificar se pode fazer a requisição
        if (requests.size() < maxRequests) {
            requests.add(now);
            return true;
        }
        
        return false;
    }
    
    /**
     * Retorna quantas requisições ainda podem ser feitas
     * 
     * @param endpoint endpoint (ex: GET /api/users)
     * @param clientIdentifier identificador do cliente
     * @param maxRequests número máximo de requisições
     * @param windowSeconds janela de tempo em segundos
     * @return número de requisições restantes
     */
    public int getRemainingRequests(String endpoint, String clientIdentifier, int maxRequests, int windowSeconds) {
        String key = endpoint + ":" + clientIdentifier;
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);
        
        List<Long> requests = requestLog.getOrDefault(key, new ArrayList<>());
        
        // Contar requisições dentro da janela
        long validRequests = requests.stream()
                .filter(timestamp -> timestamp >= windowStart)
                .count();
        
        return (int) Math.max(0, maxRequests - validRequests);
    }
    
    /**
     * Limpa registros antigos (limpeza periódica)
     */
    public void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        long oneHourAgo = now - (3600 * 1000L); // 1 hora
        
        requestLog.forEach((key, requests) -> {
            requests.removeIf(timestamp -> timestamp < oneHourAgo);
            if (requests.isEmpty()) {
                requestLog.remove(key);
            }
        });
    }
}

