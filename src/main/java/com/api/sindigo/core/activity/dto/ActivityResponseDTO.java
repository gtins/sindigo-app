package com.api.sindigo.core.activity.dto;

import com.api.sindigo.core.activity.entities.ActivityOrigin;
import com.api.sindigo.core.activity.entities.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponseDTO {

    private UUID id;

    private UUID condominiumId;

    private String title;
    private String description;
    private ActivityType type;
    private ActivityOrigin origin;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    private UUID createdById;
    private UUID ticketId;
    private UUID providerId;
}
