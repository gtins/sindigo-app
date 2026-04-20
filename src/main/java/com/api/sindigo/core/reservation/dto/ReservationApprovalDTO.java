package com.api.sindigo.core.reservation.dto;

import com.api.sindigo.core.reservation.entities.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationApprovalDTO {

    @NotNull(message = "Status é obrigatório")
    private ReservationStatus status;

    private String reason;
}

