package com.api.sindigo.core.activity.dto;

import com.api.sindigo.core.activity.entities.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ActivityCreateDTO {

    @NotBlank(message = "Title é obrigatório")
    private String title;

    @NotBlank(message = "Description é obrigatória")
    private String description;

    @NotNull(message = "Type é obrigatório")
    private ActivityType type;

    @NotNull(message = "Start date é obrigatória")
    private LocalDate startDate;

    @NotNull(message = "End date é obrigatória")
    private LocalDate endDate;
}
