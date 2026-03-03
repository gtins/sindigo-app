package com.api.sindigo.core.reservation.dto;

import com.api.sindigo.core.reservation.entities.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDTO {

    private UUID id;

    @JsonProperty("condominium_id")
    private UUID condominiumId;

    private String area;

    @JsonProperty("start_time")
    private Instant startTime;

    @JsonProperty("end_time")
    private Instant endTime;

    private ReservationStatus status;

    @JsonProperty("requested_by")
    private UUID requestedBy;

    @JsonProperty("created_at")
    private Instant createdAt;
}

