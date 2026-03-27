package com.api.sindigo.core.user.validator;

import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.validator.BaseValidator;
import org.springframework.stereotype.Component;

@Component
public class UserValidator extends BaseValidator {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int RECOMMENDED_PASSWORD_LENGTH = 8;

    public void validateUserRegistration(RegisterRequestDTO dto) {
        validateStringNotEmpty(dto.getName(), "Nome");
        validateStringNotEmpty(dto.getEmail(), "Email");
        validatePassword(dto.getPassword());
    }

    public void validatePassword(String password) {
        validateStringNotEmpty(password, "Senha");
        validateCondition(
            password.length() >= MIN_PASSWORD_LENGTH,
            "Senha deve ter no mínimo " + MIN_PASSWORD_LENGTH + " caracteres"
        );
    }

    public void validateEmailUnique(boolean emailExists) {
        validateCondition(
            !emailExists,
            "Email já cadastrado no sistema"
        );
    }
}

