package com.api.sindigo.core.ticket.validator;

import com.api.sindigo.core.ticket.dto.TicketCreateDTO;
import com.api.sindigo.core.ticket.dto.TicketUpdateDTO;
import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketValidatorTest {

    private final TicketValidator validator = new TicketValidator();

    @Test
    void validateTicketCreationAcceptsValidPayload() {
        TicketCreateDTO dto = new TicketCreateDTO(
                "Vazamento",
                "Vazamento no banheiro social",
                "Bloco A",
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                "Primeira abertura"
        );

        assertDoesNotThrow(() -> validator.validateTicketCreation(dto));
    }

    @Test
    void validateTicketCreationRejectsMissingTitle() {
        TicketCreateDTO dto = new TicketCreateDTO(
                " ",
                "Vazamento no banheiro social",
                "Bloco A",
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketCreation(dto));

        assertEquals("Título do chamado é obrigatório", exception.getMessage());
    }

    @Test
    void validateTicketCreationRejectsMissingDescription() {
        TicketCreateDTO dto = new TicketCreateDTO(
                "Vazamento",
                " ",
                "Bloco A",
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketCreation(dto));

        assertEquals("Descrição do chamado é obrigatória", exception.getMessage());
    }

    @Test
    void validateTicketCreationRejectsMissingCategory() {
        TicketCreateDTO dto = new TicketCreateDTO(
                "Vazamento",
                "Vazamento no banheiro social",
                "Bloco A",
                null,
                TicketPriority.ALTA,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketCreation(dto));

        assertEquals("Categoria do chamado é obrigatória", exception.getMessage());
    }

    @Test
    void validateTicketCreationRejectsMissingPriority() {
        TicketCreateDTO dto = new TicketCreateDTO(
                "Vazamento",
                "Vazamento no banheiro social",
                "Bloco A",
                TicketCategory.MANUTENCAO,
                null,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketCreation(dto));

        assertEquals("Prioridade do chamado é obrigatória", exception.getMessage());
    }

    @Test
    void validateTicketUpdateAcceptsValidPayload() {
        TicketUpdateDTO dto = new TicketUpdateDTO(
                "Vazamento resolvido",
                "Problema corrigido",
                "Bloco A",
                TicketStatus.FECHADO,
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                "Atualizado"
        );

        assertDoesNotThrow(() -> validator.validateTicketUpdate(dto));
    }

    @Test
    void validateTicketUpdateRejectsMissingStatus() {
        TicketUpdateDTO dto = new TicketUpdateDTO(
                "Vazamento resolvido",
                "Problema corrigido",
                "Bloco A",
                null,
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketUpdate(dto));

        assertEquals("Status do chamado é obrigatório", exception.getMessage());
    }

    @Test
    void validateTicketUpdateRejectsMissingTitle() {
        TicketUpdateDTO dto = new TicketUpdateDTO(
                " ",
                "Problema corrigido",
                "Bloco A",
                TicketStatus.FECHADO,
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketUpdate(dto));

        assertEquals("Título do chamado é obrigatório", exception.getMessage());
    }

    @Test
    void validateTicketUpdateRejectsMissingDescription() {
        TicketUpdateDTO dto = new TicketUpdateDTO(
                "Vazamento resolvido",
                " ",
                "Bloco A",
                TicketStatus.FECHADO,
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketUpdate(dto));

        assertEquals("Descrição do chamado é obrigatória", exception.getMessage());
    }

    @Test
    void validateTicketUpdateRejectsMissingCategory() {
        TicketUpdateDTO dto = new TicketUpdateDTO(
                "Vazamento resolvido",
                "Problema corrigido",
                "Bloco A",
                TicketStatus.FECHADO,
                null,
                TicketPriority.ALTA,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketUpdate(dto));

        assertEquals("Categoria do chamado é obrigatória", exception.getMessage());
    }

    @Test
    void validateTicketUpdateRejectsMissingPriority() {
        TicketUpdateDTO dto = new TicketUpdateDTO(
                "Vazamento resolvido",
                "Problema corrigido",
                "Bloco A",
                TicketStatus.FECHADO,
                TicketCategory.MANUTENCAO,
                null,
                "24h",
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validateTicketUpdate(dto));

        assertEquals("Prioridade do chamado é obrigatória", exception.getMessage());
    }
}


