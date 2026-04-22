package com.api.sindigo.core.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityContextHelper {

    /**
     * Obtém o ID do usuário autenticado a partir do contexto de segurança
     * @return UUID do usuário autenticado
     * @throws IllegalStateException se nenhum usuário está autenticado
     */
    public UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Nenhum usuário autenticado encontrado");
        }

        try {
            return UUID.fromString((String) authentication.getPrincipal());
        } catch (ClassCastException | IllegalArgumentException e) {
            throw new IllegalStateException("Erro ao extrair ID do usuário do token", e);
        }
    }

    /**
     * Obtém o email do usuário autenticado
     * @return Email do usuário
     */
    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Nenhum usuário autenticado encontrado");
        }

        return (String) authentication.getDetails();
    }
}

