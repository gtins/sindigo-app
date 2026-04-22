package com.api.sindigo.core.activity.dto;

import com.api.sindigo.core.activity.entities.ActivityOrigin;
import com.api.sindigo.core.activity.entities.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCreateDTO {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull(message = "Type é obrigatório")
    private ActivityType type;

    private ActivityOrigin origin;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate startDate;

    @NotNull(message = "Data de término é obrigatória")
    private LocalDate endDate;

    // Campos opcionais
    private UUID ticketId;
    private UUID providerId;
}
