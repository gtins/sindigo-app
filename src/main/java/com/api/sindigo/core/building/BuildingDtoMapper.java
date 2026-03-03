package com.api.sindigo.core.building;

import com.api.sindigo.core.building.dto.BuildingCreateDTO;
import com.api.sindigo.core.building.dto.BuildingResponseDTO;
import com.api.sindigo.core.building.entities.Building;
import org.springframework.stereotype.Component;

@Component
public class BuildingDtoMapper {

    public Building toDomain(BuildingCreateDTO dto) {
        return Building.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .build();
    }

    public BuildingResponseDTO toResponse(Building building) {
        return BuildingResponseDTO.builder()
                .id(building.getId())
                .name(building.getName())
                .address(building.getAddress())
                .createdAt(building.getCreatedAt())
                .build();
    }
}

