package com.api.sindigo.core.reservation;

import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.reservation.dto.ReservationResponseDTO;
import com.api.sindigo.core.reservation.entities.Reservation;
import com.api.sindigo.core.reservation.entities.ReservationStatus;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReservationDtoMapperTest {

    private final ReservationDtoMapper mapper = new ReservationDtoMapper();

    @Test
    void mapsSnapshotNameAndUnitToResponse() {
        UUID condominiumId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .condominium(Condominium.builder().id(condominiumId).build())
                .area("Salão de festas")
                .requestedByName("João Silva")
                .requestedByUnit("201")
                .startTime(LocalDateTime.of(2026, 6, 1, 18, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 22, 0))
                .status(ReservationStatus.PENDING)
                .requestedBy(User.builder().id(requesterId).name("João da conta antiga").build())
                .createdAt(LocalDateTime.of(2026, 5, 31, 12, 0))
                .build();

        ReservationResponseDTO response = mapper.toResponseDTO(reservation);

        assertEquals(reservationId, response.getId());
        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals("Salão de festas", response.getArea());
        assertEquals("João Silva", response.getRequestedByName());
        assertEquals("201", response.getRequestedByUnit());
        assertEquals(requesterId, response.getRequestedBy());
        assertEquals(ReservationStatus.PENDING, response.getStatus());
    }

    @Test
    void fallsBackToRequesterNameWhenSnapshotIsMissing() {
        Reservation reservation = Reservation.builder()
                .condominium(Condominium.builder().id(UUID.randomUUID()).build())
                .requestedBy(User.builder().id(UUID.randomUUID()).name("Maria Souza").build())
                .build();

        ReservationResponseDTO response = mapper.toResponseDTO(reservation);

        assertEquals("Maria Souza", response.getRequestedByName());
        assertNull(response.getRequestedByUnit());
    }
}

