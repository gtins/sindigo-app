package com.api.sindigo.core.reservation.dto;

import com.api.sindigo.core.reservation.entities.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDTO {

    private UUID id;

    private UUID condominiumId;

    private String area;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private ReservationStatus status;

    private UUID requestedBy;

    private LocalDateTime createdAt;
}

