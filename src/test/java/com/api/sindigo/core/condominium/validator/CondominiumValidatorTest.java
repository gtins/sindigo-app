package com.api.sindigo.core.condominium.validator;

import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CondominiumValidatorTest {

    private final CondominiumValidator validator = new CondominiumValidator();

    @Test
    void validateCondominiumCreationAcceptsValidPayload() {
        CondominiumCreateDTO dto = CondominiumCreateDTO.builder()
                .name("Residencial Alfa")
                .address("Rua A, 123")
                .unidades(10)
                .build();

        assertDoesNotThrow(() -> validator.validateCondominiumCreation(dto));
    }

    @Test
    void validateCondominiumCreationRejectsMissingName() {
        CondominiumCreateDTO dto = CondominiumCreateDTO.builder()
                .address("Rua A, 123")
                .unidades(10)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateCondominiumCreation(dto)
        );

        assertEquals("Nome não pode ser vazio ou nulo", exception.getMessage());
    }

    @Test
    void validateCondominiumCreationRejectsMissingUnidades() {
        CondominiumCreateDTO dto = CondominiumCreateDTO.builder()
                .name("Residencial Alfa")
                .address("Rua A, 123")
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateCondominiumCreation(dto)
        );

        assertEquals("Unidades não pode ser nulo", exception.getMessage());
    }
}

