package com.api.sindigo.core.reservation;

import com.api.sindigo.core.reservation.dto.ReservationApprovalDTO;
import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.core.reservation.dto.ReservationResponseDTO;
import com.api.sindigo.core.reservation.entities.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationControllerTest {

    private final ReservationService reservationService = mock(ReservationService.class);
    private final ReservationController controller = new ReservationController(reservationService);

    @Test
    void createReservationReturnsCreatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        ReservationCreateDTO dto = ReservationCreateDTO.builder()
                .area("Salão de festas")
                .unitNumber("201")
                .startTime(LocalDateTime.of(2026, 6, 1, 18, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 22, 0))
                .build();
        ReservationResponseDTO response = buildResponse(condominiumId, ReservationStatus.PENDING);

        when(reservationService.createReservation(condominiumId, dto)).thenReturn(response);

        var result = controller.createReservation(condominiumId, dto);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(reservationService).createReservation(condominiumId, dto);
    }

    @Test
    void listReservationsWithoutStatusDelegatesToGeneralListing() {
        UUID condominiumId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "startTime"));
        ReservationResponseDTO response = buildResponse(condominiumId, ReservationStatus.PENDING);
        Page<ReservationResponseDTO> page = new PageImpl<>(List.of(response), pageable, 1);

        when(reservationService.listByCondominium(condominiumId, pageable)).thenReturn(page);

        var result = controller.listReservations(condominiumId, null, pageable);

        assertEquals(200, result.getStatusCode().value());
        Page<ReservationResponseDTO> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.getTotalElements());
        assertEquals(response, body.getContent().getFirst());
    }

    @Test
    void listReservationsWithStatusDelegatesToFilteredListing() {
        UUID condominiumId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "startTime"));
        ReservationResponseDTO response = buildResponse(condominiumId, ReservationStatus.CONFIRMED);
        Page<ReservationResponseDTO> page = new PageImpl<>(List.of(response), pageable, 1);

        when(reservationService.listByCondominiumAndStatus(condominiumId, ReservationStatus.CONFIRMED, pageable)).thenReturn(page);

        var result = controller.listReservations(condominiumId, ReservationStatus.CONFIRMED, pageable);

        assertEquals(200, result.getStatusCode().value());
        Page<ReservationResponseDTO> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.getTotalElements());
        assertEquals(response, body.getContent().getFirst());
    }

    @Test
    void approveReservationReturnsUpdatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        ReservationApprovalDTO dto = new ReservationApprovalDTO(ReservationStatus.CONFIRMED, "ok");
        ReservationResponseDTO response = buildResponse(condominiumId, ReservationStatus.CONFIRMED);
        response.setId(reservationId);

        when(reservationService.approveReservation(eq(condominiumId), eq(reservationId), eq(dto))).thenReturn(response);

        var result = controller.approveReservation(condominiumId, reservationId, dto);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void checkAvailabilityReturnsServiceMap() {
        UUID condominiumId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 1);
        Map<String, Object> availability = Map.of(
                "condominiumId", condominiumId,
                "area", "Salão de festas",
                "date", date,
                "available", true,
                "conflictsFound", 0
        );

        when(reservationService.checkAvailability(condominiumId, "Salão de festas", date)).thenReturn(availability);

        var result = controller.checkAvailability(condominiumId, "Salão de festas", date);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(availability, result.getBody());
    }

    private ReservationResponseDTO buildResponse(UUID condominiumId, ReservationStatus status) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setCondominiumId(condominiumId);
        dto.setArea("Salão de festas");
        dto.setRequestedByName("Maria Souza");
        dto.setRequestedByUnit("201");
        dto.setStartTime(LocalDateTime.of(2026, 6, 1, 18, 0));
        dto.setEndTime(LocalDateTime.of(2026, 6, 1, 22, 0));
        dto.setStatus(status);
        dto.setRequestedBy(UUID.randomUUID());
        dto.setCreatedAt(LocalDateTime.of(2026, 6, 1, 12, 0));
        return dto;
    }
}

