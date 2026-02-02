package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/building/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    // CREATE
    @PostMapping
    public ActivityResponseDTO create(
            @RequestBody ActivityCreateDTO dto
    ) {
        return activityService.create(dto);
    }

    // READ
    @GetMapping
    public List<ActivityResponseDTO> list(
            @RequestParam Long buildingId
    ) {
        return activityService.listByBuilding(buildingId);
    }
}
