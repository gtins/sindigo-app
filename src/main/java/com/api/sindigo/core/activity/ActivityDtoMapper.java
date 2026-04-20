package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;

public class ActivityDtoMapper {

    private ActivityDtoMapper() {
    }

    public static ActivityResponseDTO toResponseDTO(Activity activity) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(activity.getId());
        dto.setCondominiumId(activity.getCondominium().getId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setType(activity.getType());
        dto.setOrigin(activity.getOrigin());
        dto.setStartDate(activity.getStartDate());
        dto.setEndDate(activity.getEndDate());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        dto.setCreatedById(activity.getCreatedBy().getId());
        if (activity.getTicket() != null) {
            dto.setTicketId(activity.getTicket().getId());
        }
        if (activity.getProvider() != null) {
            dto.setProviderId(activity.getProvider().getId());
        }
        return dto;
    }
}


