package com.api.sindigo.core.reservation;

import com.api.sindigo.core.reservation.dto.ReservationResponseDTO;
import com.api.sindigo.core.reservation.entities.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationDtoMapper {

    public ReservationResponseDTO toResponseDTO(Reservation reservation) {
        return ReservationResponseDTO.builder()
                .id(reservation.getId())
                .condominiumId(reservation.getCondominium().getId())
                .area(reservation.getArea())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .requestedBy(reservation.getRequestedBy() != null ? reservation.getRequestedBy().getId() : null)
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}

