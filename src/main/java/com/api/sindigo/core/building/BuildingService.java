package com.api.sindigo.core.building;

import com.api.sindigo.core.building.entities.Building;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;

    // CREATE
    public Building createBuilding(Building building) {
        return buildingRepository.save(building);
    }

    // READ
    public List<Building> listBuildings() {
        return buildingRepository.findAll();
    }

    // READ
    public Building getById(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found"));
    }
}
