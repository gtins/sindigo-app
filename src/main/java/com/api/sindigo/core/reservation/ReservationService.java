package com.api.sindigo.core.reservation;

import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.core.reservation.dto.ReservationResponseDTO;
import com.api.sindigo.core.reservation.entities.Reservation;
import com.api.sindigo.core.reservation.entities.ReservationStatus;
import com.api.sindigo.core.reservation.validator.ReservationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CondominiumRepository condominiumRepository;
    private final ReservationDtoMapper reservationDtoMapper;
    private final ReservationValidator reservationValidator;

    @Transactional
    public ReservationResponseDTO createReservation(UUID condominiumId, ReservationCreateDTO dto) {
        reservationValidator.validateReservationCreation(dto);

        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found with id: " + condominiumId));

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                condominiumId,
                dto.getArea(),
                dto.getStartTime(),
                dto.getEndTime()
        );

        reservationValidator.validateNoConflicts(!conflicts.isEmpty());

        Reservation reservation = new Reservation();
        reservation.setCondominium(condominium);
        reservation.setArea(dto.getArea());
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());
        reservation.setStatus(ReservationStatus.PENDING);

        Reservation saved = reservationRepository.save(reservation);

        return reservationDtoMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> listByCondominiumAndStatus(UUID condominiumId, ReservationStatus status, Pageable pageable) {
        Page<Reservation> page = reservationRepository.findByCondominiumIdAndStatusOrderByStartTimeDesc(
                condominiumId,
                status,
                pageable
        );
        return page.map(reservationDtoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> listByCondominium(UUID condominiumId, Pageable pageable) {
        Page<Reservation> page = reservationRepository.findByCondominiumIdOrderByStartTimeDesc(
                condominiumId,
                pageable
        );
        return page.map(reservationDtoMapper::toResponseDTO);
    }
}
