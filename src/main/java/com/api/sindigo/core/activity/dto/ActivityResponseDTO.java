package com.api.sindigo.core.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private LocalDateTime createdAt;
}
