package com.api.sindigo.core.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationAvailabilityDTO {

    private UUID condominiumId;
    private String area;
    private LocalDate date;
    private boolean available;
    private int conflictsFound;
}

