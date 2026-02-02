package com.api.sindigo.core.building;

import com.api.sindigo.core.building.entities.Building;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    // CREATE
    @PostMapping
    public Building create(@RequestBody Building building) {
        return buildingService.createBuilding(building);
    }

    // READ
    @GetMapping
    public List<Building> list() {
        return buildingService.listBuildings();
    }

    // READ
    @GetMapping("/{id}")
    public Building getById(@PathVariable Long id) {
        return buildingService.getById(id);
    }
}
