package com.api.sindigo.core.reservation;

import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.reservation.dto.ReservationApprovalDTO;
import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.core.reservation.dto.ReservationResponseDTO;
import com.api.sindigo.core.reservation.entities.Reservation;
import com.api.sindigo.core.reservation.entities.ReservationStatus;
import com.api.sindigo.core.reservation.validator.ReservationValidator;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String CONDOMINIUM_ACCESS_ERROR_MESSAGE =
            "Condominium not found or you don't have access";

    private static final String AUTHENTICATED_USER_NOT_FOUND_MESSAGE =
            "Usuário autenticado não encontrado";

    private static final String RESERVATION_NOT_FOUND_MESSAGE = "Reservation not found";
    private static final String ONLY_PENDING_RESERVATIONS_CAN_BE_APPROVED_MESSAGE =
            "Only PENDING reservations can be approved";

    private static final String CONDOMINIUM_ID_KEY = "condominiumId";
    private static final String AREA_KEY = "area";
    private static final String DATE_KEY = "date";
    private static final String AVAILABLE_KEY = "available";
    private static final String CONFLICTS_FOUND_KEY = "conflictsFound";

    private final ReservationRepository reservationRepository;
    private final CondominiumRepository condominiumRepository;
    private final ReservationDtoMapper reservationDtoMapper;
    private final ReservationValidator reservationValidator;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @Transactional
    public ReservationResponseDTO createReservation(UUID condominiumId, ReservationCreateDTO dto) {
        reservationValidator.validateReservationCreation(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        Condominium condominium = condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        User requester = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException(AUTHENTICATED_USER_NOT_FOUND_MESSAGE));

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
        reservation.setRequestedByName(requester.getName());
        reservation.setRequestedByUnit(dto.getUnitNumber());
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setRequestedBy(requester);

        Reservation saved = reservationRepository.save(reservation);

        return reservationDtoMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> listByCondominiumAndStatus(
            UUID condominiumId,
            ReservationStatus status,
            Pageable pageable
    ) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        Condominium condominium = condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        boolean isOwner = condominium.getOwner().getId().equals(authenticatedUserId);

        Page<Reservation> page;
        if (isOwner) {
            page = reservationRepository.findByCondominiumIdAndStatusOrderByStartTimeDesc(
                    condominiumId,
                    status,
                    pageable
            );
        } else {
            page = reservationRepository.findByCondominiumIdAndStatusAndRequestedByIdOrderByStartTimeDesc(
                    condominiumId,
                    status,
                    authenticatedUserId,
                    pageable
            );
        }

        return page.map(reservationDtoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> listByCondominium(UUID condominiumId, Pageable pageable) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        Condominium condominium = condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        boolean isOwner = condominium.getOwner().getId().equals(authenticatedUserId);

        Page<Reservation> page;
        if (isOwner) {
            page = reservationRepository.findByCondominiumIdOrderByStartTimeDesc(
                    condominiumId,
                    pageable
            );
        } else {
            page = reservationRepository.findByCondominiumIdAndRequestedByIdOrderByStartTimeDesc(
                    condominiumId,
                    authenticatedUserId,
                    pageable
            );
        }

        return page.map(reservationDtoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkAvailability(UUID condominiumId, String area, LocalDate date) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                condominiumId,
                area,
                startOfDay,
                endOfDay
        );

        return Map.of(
                CONDOMINIUM_ID_KEY, condominiumId,
                AREA_KEY, area,
                DATE_KEY, date,
                AVAILABLE_KEY, conflicts.isEmpty(),
                CONFLICTS_FOUND_KEY, conflicts.size()
        );
    }

    @Transactional
    public ReservationResponseDTO approveReservation(
            UUID condominiumId,
            UUID reservationId,
            ReservationApprovalDTO dto
    ) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Reservation reservation = reservationRepository.findByIdAndCondominiumId(reservationId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND_MESSAGE));

        if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
            throw new IllegalStateException(ONLY_PENDING_RESERVATIONS_CAN_BE_APPROVED_MESSAGE);
        }

        reservation.setStatus(dto.getStatus());

        Reservation saved = reservationRepository.save(reservation);

        return reservationDtoMapper.toResponseDTO(saved);
    }
}