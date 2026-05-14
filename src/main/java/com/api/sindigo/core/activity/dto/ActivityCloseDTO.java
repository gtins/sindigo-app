package com.api.sindigo.core.activity.dto;

import com.api.sindigo.core.activity.entities.ActivityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCloseDTO {

    @NotNull(message = "Status is required")
    private ActivityStatus status;  // COMPLETED ou CANCELLED

    private String closingNotes;    // Notas de encerramento
}

