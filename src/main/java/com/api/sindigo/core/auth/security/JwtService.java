package com.api.sindigo.core.auth.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Valida o token e retorna informações sobre sua validade
     * @param token Token JWT
     * @return Informações sobre a validação
     */
    public JwtValidationResponse validateToken(String token) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                return new JwtValidationResponse(false, "Token inválido", null);
            }

            long expirationTime = jwtTokenProvider.getExpirationTime();
            Date expiresAt = new Date(System.currentTimeMillis() + expirationTime);

            return new JwtValidationResponse(true, "Token válido", expiresAt);
        } catch (Exception e) {
            return new JwtValidationResponse(false, "Token expirado ou inválido", null);
        }
    }

    /**
     * DTO para resposta de validação de token
     */
    @Getter
    public static class JwtValidationResponse {
        public boolean valid;
        public String reason;
        public Date expiresAt;

        public JwtValidationResponse(boolean valid, String reason, Date expiresAt) {
            this.valid = valid;
            this.reason = reason;
            this.expiresAt = expiresAt;
        }

        public boolean isValid() {
            return valid;
        }
    }
}

