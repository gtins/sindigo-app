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

    @Test
    void mapsApprovedStatus() {
        UUID condominiumId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .condominium(Condominium.builder().id(condominiumId).build())
                .area("Quadra")
                .requestedByUnit("101")
                .startTime(LocalDateTime.of(2026, 6, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 6, 10, 11, 0))
                .status(ReservationStatus.CONFIRMED)
                .requestedBy(User.builder().id(UUID.randomUUID()).build())
                .createdAt(LocalDateTime.now())
                .build();

        ReservationResponseDTO response = mapper.toResponseDTO(reservation);

        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void mapsRejectedStatus() {
        UUID condominiumId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .condominium(Condominium.builder().id(condominiumId).build())
                .area("Piscina")
                .requestedByUnit("202")
                .startTime(LocalDateTime.of(2026, 6, 15, 14, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 16, 0))
                .status(ReservationStatus.CANCELLED)
                .requestedBy(User.builder().id(UUID.randomUUID()).build())
                .createdAt(LocalDateTime.now())
                .build();

        ReservationResponseDTO response = mapper.toResponseDTO(reservation);

        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
    }

    @Test
    void mapsNullRequestedByName() {
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .condominium(Condominium.builder().id(condominiumId).build())
                .requestedByName(null)
                .requestedBy(User.builder().id(userId).name("John").build())
                .build();

        ReservationResponseDTO response = mapper.toResponseDTO(reservation);

        assertEquals("John", response.getRequestedByName());
    }

    @Test
    void mapsNullRequestedBy() {
        UUID condominiumId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .condominium(Condominium.builder().id(condominiumId).build())
                .requestedByName("Guest User")
                .requestedBy(null)
                .build();

        ReservationResponseDTO response = mapper.toResponseDTO(reservation);

        assertEquals("Guest User", response.getRequestedByName());
        assertNull(response.getRequestedBy());
    }

    @Test
    void mapsCompleteReservationWithAllFields() {
        UUID reservationId = UUID.randomUUID();
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 25, 10, 0);
        LocalDateTime startTime = LocalDateTime.of(2026, 6, 20, 18, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 6, 20, 22, 0);

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .condominium(Condominium.builder().id(condominiumId).build())
                .area("Salão Completo")
                .requestedByName("Carlos Alberto")
                .requestedByUnit("305")
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.CONFIRMED)
                .requestedBy(User.builder().id(userId).build())
                .createdAt(createdAt)
                .build();

        ReservationResponseDTO response = mapper.toResponseDTO(reservation);

        assertEquals(reservationId, response.getId());
        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals("Salão Completo", response.getArea());
        assertEquals("Carlos Alberto", response.getRequestedByName());
        assertEquals("305", response.getRequestedByUnit());
        assertEquals(startTime, response.getStartTime());
        assertEquals(endTime, response.getEndTime());
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
        assertEquals(userId, response.getRequestedBy());
        assertEquals(createdAt, response.getCreatedAt());
    }
}

