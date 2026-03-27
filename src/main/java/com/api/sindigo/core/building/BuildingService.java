package com.api.sindigo.core.building;

// DEPRECATED: Use com.api.sindigo.core.condominium.CondominiumService instead
// This class is kept for reference only and should be deleted

/*
import com.api.sindigo.core.building.dto.BuildingCreateDTO;
import com.api.sindigo.core.building.dto.BuildingResponseDTO;
import com.api.sindigo.core.building.entities.Building;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final BuildingDtoMapper buildingDtoMapper;

    // CREATE
    public BuildingResponseDTO createBuilding(BuildingCreateDTO dto) {
        Building building = buildingDtoMapper.toDomain(dto);
        Building savedBuilding = buildingRepository.save(building);
        return buildingDtoMapper.toResponse(savedBuilding);
    }

    // READ
    public List<BuildingResponseDTO> listBuildings() {
        return buildingRepository.findAll()
                .stream()
                .map(buildingDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    // READ
    public BuildingResponseDTO getById(UUID id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found"));
        return buildingDtoMapper.toResponse(building);
    }
}
*/



