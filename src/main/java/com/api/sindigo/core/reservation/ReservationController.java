package com.api.sindigo.core.reservation;

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

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // CREATE - POST /reservations
    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @RequestParam UUID condominiumId,
            @Valid @RequestBody ReservationCreateDTO dto
    ) {
        ReservationResponseDTO response = reservationService.createReservation(condominiumId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // LIST with optional status filter - GET /condominiums/{id}/reservations
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
}
