package com.api.sindigo.validator;

import com.api.sindigo.exception.ResourceNotFoundException;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseValidatorTest {

    private final TestValidator validator = new TestValidator();

    @Test
    void validateNotNullReturnsTheSameInstance() {
        String value = "ok";

        String result = validator.notNull(value, "mensagem");

        assertSame(value, result);
    }

    @Test
    void validateNotNullThrowsResourceNotFoundWhenValueIsNull() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> validator.notNull(null, "não encontrado")
        );

        assertEquals("não encontrado", exception.getMessage());
    }

    @Test
    void validateConditionThrowsValidationExceptionWhenConditionIsFalse() {
        boolean condition = false;
        String message = "condição inválida";

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.condition(condition, message)
        );

        assertEquals(message, exception.getMessage());
    }

    @Test
    void validateStringNotEmptyRejectsBlankAndNullValues() {
        String fieldName = "Campo";
        String blankValue = "   ";
        String nullValue = null;

        ValidationException blankException = assertThrows(
                ValidationException.class,
                () -> validator.stringNotEmpty(blankValue, fieldName)
        );

        ValidationException nullException = assertThrows(
                ValidationException.class,
                () -> validator.stringNotEmpty(nullValue, fieldName)
        );

        assertEquals(fieldName + " não pode ser vazio ou nulo", blankException.getMessage());
        assertEquals(fieldName + " não pode ser vazio ou nulo", nullException.getMessage());
    }

    private static class TestValidator extends BaseValidator {
        String notNull(String value, String message) {
            return validateNotNull(value, message);
        }

        void condition(boolean condition, String message) {
            validateCondition(condition, message);
        }

        void stringNotEmpty(String value, String fieldName) {
            validateStringNotEmpty(value, fieldName);
        }
    }
}


