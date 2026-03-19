package com.api.sindigo.core.activity.dto;

import com.api.sindigo.core.activity.entities.ActivityType;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ActivityResponseDTO {

    private UUID id;

    private UUID condominiumId;

    private String title;
    private String description;
    private ActivityType type;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate createdAt;
}
