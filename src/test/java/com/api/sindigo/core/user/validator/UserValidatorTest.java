package com.api.sindigo.core.user.validator;

import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValidatorTest {

    private final UserValidator validator = new UserValidator();

    @Test
    void validateUserRegistrationAcceptsValidPayload() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("João Silva")
                .email("joao@example.com")
                .password("senha123")
                .build();

        assertDoesNotThrow(() -> validator.validateUserRegistration(dto));
    }

    @Test
    void validateUserRegistrationRejectsMissingEmail() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("João Silva")
                .password("senha123")
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserRegistration(dto)
        );

        assertEquals("Email não pode ser vazio ou nulo", exception.getMessage());
    }

    @Test
    void validatePasswordRejectsShortPassword() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validatePassword("123")
        );

        assertEquals("Senha deve ter no mínimo 6 caracteres", exception.getMessage());
    }

    @Test
    void validateEmailUniqueRejectsExistingEmail() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateEmailUnique(true)
        );

        assertEquals("Email já cadastrado no sistema", exception.getMessage());
    }
}

