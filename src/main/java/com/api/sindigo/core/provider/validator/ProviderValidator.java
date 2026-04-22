package com.api.sindigo.core.provider.validator;

import com.api.sindigo.core.provider.dto.ProviderCreateDTO;
import com.api.sindigo.core.provider.dto.ProviderUpdateDTO;
import org.springframework.stereotype.Component;

@Component
public class ProviderValidator {

    public void validateProviderCreation(ProviderCreateDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Nome do prestador é obrigatório");
        }

        if (dto.getServiceType() == null || dto.getServiceType().isBlank()) {
            throw new IllegalArgumentException("Tipo de serviço é obrigatório");
        }

        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            validateEmail(dto.getEmail());
        }
    }

    public void validateProviderUpdate(ProviderUpdateDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Nome do prestador é obrigatório");
        }

        if (dto.getServiceType() == null || dto.getServiceType().isBlank()) {
            throw new IllegalArgumentException("Tipo de serviço é obrigatório");
        }

        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            validateEmail(dto.getEmail());
        }
    }

    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email inválido");
        }
    }
}

