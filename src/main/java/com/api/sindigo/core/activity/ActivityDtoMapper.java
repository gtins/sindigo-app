package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;

import java.time.LocalDateTime;

public class ActivityDtoMapper {

    private ActivityDtoMapper() {
        // impede instanciação
    }

    public static ActivityResponseDTO toResponseDTO(Activity activity) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(activity.getId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setCompleted(activity.getInstances().stream()
                .allMatch(instance -> instance.getStatus() == com.api.sindigo.core.activity.entities.ActivityStatus.COMPLETED));
        dto.setCreatedAt(LocalDateTime.from(activity.getCreatedAt()));
        return dto;
    }
}
