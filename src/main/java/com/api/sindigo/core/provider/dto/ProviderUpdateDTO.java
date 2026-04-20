package com.api.sindigo.core.provider.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderUpdateDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Tipo de serviço é obrigatório")
    private String serviceType;

    @NotBlank(message = "Telefone é obrigatório")
    private String phone;

    @Email(message = "Email deve ser válido")
    private String email;

    private String notes;
}

