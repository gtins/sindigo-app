package com.api.sindigo.core.provider.validator;

import com.api.sindigo.core.provider.dto.ProviderCreateDTO;
import com.api.sindigo.core.provider.dto.ProviderUpdateDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderValidatorTest {

    private final ProviderValidator validator = new ProviderValidator();

    @Test
    void validateProviderCreationAcceptsValidPayload() {
        ProviderCreateDTO dto = new ProviderCreateDTO(
                "Carlos Manutenção",
                "Elétrica",
                "11999999999",
                "carlos@example.com",
                null
        );

        assertDoesNotThrow(() -> validator.validateProviderCreation(dto));
    }

    @Test
    void validateProviderCreationRejectsInvalidEmail() {
        ProviderCreateDTO dto = new ProviderCreateDTO(
                "Carlos Manutenção",
                "Elétrica",
                "11999999999",
                "email-inválido",
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateProviderCreation(dto)
        );

        assertEquals("Email inválido", exception.getMessage());
    }

    @Test
    void validateProviderUpdateRejectsMissingPhone() {
        ProviderUpdateDTO dto = new ProviderUpdateDTO(
                "Carlos Manutenção",
                "Elétrica",
                "   ",
                "carlos@example.com",
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateProviderUpdate(dto)
        );

        assertEquals("Telefone é obrigatório", exception.getMessage());
    }
}

