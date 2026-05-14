package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCloseDTO;
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

    @PostMapping("/condominiums/{id}/activities")
    public ResponseEntity<ActivityResponseDTO> createActivity(
            @PathVariable UUID id,
            @Valid @RequestBody ActivityCreateDTO dto
    ) {
        ActivityResponseDTO response = activityService.addActivity(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/condominiums/{id}/activities")
    public List<ActivityResponseDTO> list(
            @PathVariable UUID id
    ) {
        return activityService.listByCondominium(id);
    }

    @PostMapping("/condominiums/{condominiumId}/activities/{activityId}/close")
    public ResponseEntity<ActivityResponseDTO> closeActivity(
            @PathVariable UUID condominiumId,
            @PathVariable UUID activityId,
            @Valid @RequestBody ActivityCloseDTO dto
    ) {
        ActivityResponseDTO response = activityService.closeActivity(condominiumId, activityId, dto);
        return ResponseEntity.ok(response);
    }
}
