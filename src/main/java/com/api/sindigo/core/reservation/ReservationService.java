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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

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

        // Verifica se o condomínio pertence ao usuário autenticado (owner ou member)
        Condominium condominium = condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        User requester = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));

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
        reservation.setRequestedBy(requester);

        Reservation saved = reservationRepository.save(reservation);

        return reservationDtoMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> listByCondominiumAndStatus(UUID condominiumId, ReservationStatus status, Pageable pageable) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        Condominium condominium = condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        // Se o usuário é owner (síndico), retorna TODAS as reservas
        // Se não for owner, retorna apenas as SUAS reservas
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

        // Verifica se o condomínio pertence ao usuário autenticado (owner ou member)
        Condominium condominium = condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        // Se o usuário é owner (síndico), retorna TODAS as reservas
        // Se não for owner, retorna apenas as SUAS reservas
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

    @Transactional
    public ReservationResponseDTO approveReservation(UUID condominiumId, UUID reservationId, ReservationApprovalDTO dto) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        Reservation reservation = reservationRepository.findByIdAndCondominiumId(reservationId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
            throw new IllegalStateException("Only PENDING reservations can be approved");
        }

        reservation.setStatus(dto.getStatus());

        Reservation saved = reservationRepository.save(reservation);

        return reservationDtoMapper.toResponseDTO(saved);
    }
}
