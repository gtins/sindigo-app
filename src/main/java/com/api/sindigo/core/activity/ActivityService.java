package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.building.BuildingRepository;
import com.api.sindigo.core.building.entities.Building;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final BuildingRepository buildingRepository;

    // CREATE
    public ActivityResponseDTO create(ActivityCreateDTO dto) {

        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found"));

        Activity activity = new Activity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setBuilding(building);

        Activity saved = activityRepository.save(activity);

        return toResponseDTO(saved);
    }

    // LIST
    public List<ActivityResponseDTO> listByBuilding(Long buildingId) {
        return activityRepository.findByBuildingId(buildingId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // Mapper simples
    private ActivityResponseDTO toResponseDTO(Activity activity) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(activity.getId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setCompleted(activity.getCompleted());
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }
}
