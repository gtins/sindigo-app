package com.api.sindigo.core.building;

// DEPRECATED: Use com.api.sindigo.core.condominium.CondominiumController instead
// This class is kept for reference only and should be deleted

/*
import com.api.sindigo.core.building.dto.BuildingCreateDTO;
import com.api.sindigo.core.building.dto.BuildingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/condominiums")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    // CREATE
    @PostMapping
    public ResponseEntity<BuildingResponseDTO> create(@Valid @RequestBody BuildingCreateDTO dto) {
        BuildingResponseDTO response = buildingService.createBuilding(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<BuildingResponseDTO>> list() {
        List<BuildingResponseDTO> response = buildingService.listBuildings();
        return ResponseEntity.ok(response);
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<BuildingResponseDTO> getById(@PathVariable UUID id) {
        BuildingResponseDTO response = buildingService.getById(id);
        return ResponseEntity.ok(response);
    }
}
*/



