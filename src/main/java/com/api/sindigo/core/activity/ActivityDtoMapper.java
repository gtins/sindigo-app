package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;

public class ActivityDtoMapper {

    private ActivityDtoMapper() {
        // impede instanciação
    }

    public static ActivityResponseDTO toResponseDTO(Activity activity) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(activity.getId());
        dto.setBuildingId(activity.getBuilding().getId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setType(activity.getType());
        dto.setStartDate(activity.getStartDate());
        dto.setEndDate(activity.getEndDate());
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }
}
