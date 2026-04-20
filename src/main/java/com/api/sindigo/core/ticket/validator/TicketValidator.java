package com.api.sindigo.core.ticket.validator;

import com.api.sindigo.core.ticket.dto.TicketCreateDTO;
import com.api.sindigo.core.ticket.dto.TicketUpdateDTO;
import org.springframework.stereotype.Component;

@Component
public class TicketValidator {

    public void validateTicketCreation(TicketCreateDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Título do chamado é obrigatório");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Descrição do chamado é obrigatória");
        }

        if (dto.getCategory() == null) {
            throw new IllegalArgumentException("Categoria do chamado é obrigatória");
        }

        if (dto.getPriority() == null) {
            throw new IllegalArgumentException("Prioridade do chamado é obrigatória");
        }
    }

    public void validateTicketUpdate(TicketUpdateDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Título do chamado é obrigatório");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Descrição do chamado é obrigatória");
        }

        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("Status do chamado é obrigatório");
        }

        if (dto.getCategory() == null) {
            throw new IllegalArgumentException("Categoria do chamado é obrigatória");
        }

        if (dto.getPriority() == null) {
            throw new IllegalArgumentException("Prioridade do chamado é obrigatória");
        }
    }
}

