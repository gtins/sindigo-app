package com.api.sindigo.core.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ActivityResponseDTO {

    private UUID id;
    private String title;
    private String description;
    private Boolean completed;
    private LocalDateTime createdAt;
}
