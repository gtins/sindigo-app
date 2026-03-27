package com.api.sindigo.core.condominium.validator;

import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.validator.BaseValidator;
import org.springframework.stereotype.Component;

@Component
public class CondominiumValidator extends BaseValidator {

    public void validateCondominiumCreation(CondominiumCreateDTO dto) {
        validateStringNotEmpty(dto.getName(), "Nome");
        validateStringNotEmpty(dto.getAddress(), "Endereço");
    }
}

