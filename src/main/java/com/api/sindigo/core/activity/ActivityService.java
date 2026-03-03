package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.building.BuildingRepository;
import com.api.sindigo.core.building.entities.Building;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final BuildingRepository buildingRepository;

    // CREATE - Para endpoint POST /condominiums/{id}/activities
    public ActivityResponseDTO addActivity(UUID buildingId, ActivityCreateDTO dto) {
        // Buscar condomínio
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building not found with id: " + buildingId));

        // Validar start_date < end_date
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Vincular entidade
        Activity activity = new Activity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setBuilding(building);

        // Salvar atividade
        Activity saved = activityRepository.save(activity);

        return ActivityDtoMapper.toResponseDTO(saved);
    }

    // LIST
    public List<ActivityResponseDTO> listByBuilding(UUID buildingId) {
        return activityRepository.findByBuildingId(buildingId)
                .stream()
                .map(ActivityDtoMapper::toResponseDTO)
                .toList();
    }
}
