package com.api.sindigo.core.activity.dto;

import com.api.sindigo.core.activity.entities.ActivityType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ActivityResponseDTO {

    private UUID id;

    @JsonProperty("condominium_id")
    private UUID condominiumId;

    private String title;
    private String description;
    private ActivityType type;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("created_at")
    private Instant createdAt;
}
