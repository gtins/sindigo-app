package com.api.sindigo.core.reservation;

import com.api.sindigo.core.reservation.dto.ReservationApprovalDTO;
import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.core.reservation.dto.ReservationResponseDTO;
import com.api.sindigo.core.reservation.entities.ReservationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/condominiums/{id}/reservations")
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @PathVariable UUID id,
            @Valid @RequestBody ReservationCreateDTO dto
    ) {
        ReservationResponseDTO response = reservationService.createReservation(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/condominiums/{id}/reservations")
    public ResponseEntity<Page<ReservationResponseDTO>> listReservations(
            @PathVariable UUID id,
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReservationResponseDTO> response;

        if (status != null) {
            response = reservationService.listByCondominiumAndStatus(id, status, pageable);
        } else {
            response = reservationService.listByCondominium(id, pageable);
        }

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/condominiums/{condominiumId}/reservations/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> approveReservation(
            @PathVariable UUID condominiumId,
            @PathVariable UUID reservationId,
            @Valid @RequestBody ReservationApprovalDTO dto
    ) {
        ReservationResponseDTO response = reservationService.approveReservation(condominiumId, reservationId, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/condominiums/{condominiumId}/reservations/{reservationId}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(
            @PathVariable UUID condominiumId,
            @PathVariable UUID reservationId
    ) {
        ReservationResponseDTO response = reservationService.cancelReservation(condominiumId, reservationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/condominiums/{id}/reservations/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable UUID id,
            @RequestParam String area,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(reservationService.checkAvailability(id, area, date));
    }
}
