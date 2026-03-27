package com.api.sindigo.validator;

import com.api.sindigo.exception.ResourceNotFoundException;
import com.api.sindigo.exception.ValidationException;

public class BaseValidator {

    protected <T> T validateNotNull(T object, String message) {
        if (object == null) {
            throw new ResourceNotFoundException(message);
        }
        return object;
    }

    protected void validateCondition(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    protected void validateStringNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " não pode ser vazio ou nulo");
        }
    }
}

