package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    // CREATE - POST /condominiums/{id}/activities
    @PostMapping("/condominiums/{id}/activities")
    public ResponseEntity<ActivityResponseDTO> createActivity(
            @PathVariable UUID id,
            @Valid @RequestBody ActivityCreateDTO dto
    ) {
        ActivityResponseDTO response = activityService.addActivity(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // LIST - GET /building/activities
    @GetMapping("/building/activities")
    public List<ActivityResponseDTO> list(
            @RequestParam UUID buildingId
    ) {
        return activityService.listByBuilding(buildingId);
    }
}
