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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final CondominiumRepository condominiumRepository = mock(CondominiumRepository.class);
    private final ReservationDtoMapper reservationDtoMapper = new ReservationDtoMapper();
    private final ReservationValidator reservationValidator = new ReservationValidator();
    private final SecurityContextHelper securityContextHelper = mock(SecurityContextHelper.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ReservationService reservationService = new ReservationService(
            reservationRepository,
            condominiumRepository,
            reservationDtoMapper,
            reservationValidator,
            securityContextHelper,
            userRepository
    );

    private UUID authenticatedUserId;
    private UUID condominiumId;
    private Condominium condominium;
    private User requester;

    @BeforeEach
    void setUp() {
        authenticatedUserId = UUID.randomUUID();
        condominiumId = UUID.randomUUID();
        requester = User.builder()
                .id(authenticatedUserId)
                .name("Maria Souza")
                .email("maria@example.com")
                .build();
        condominium = Condominium.builder()
                .id(condominiumId)
                .owner(User.builder().id(authenticatedUserId).build())
                .build();
    }

    @Test
    void createReservationPersistsNewReservation() {
        ReservationCreateDTO dto = ReservationCreateDTO.builder()
                .area("Salão de festas")
                .unitNumber("201")
                .startTime(LocalDateTime.of(2026, 6, 9, 18, 0))
                .endTime(LocalDateTime.of(2026, 6, 9, 22, 0))
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(requester));
        when(reservationRepository.findConflictingReservations(condominiumId, dto.getArea(), dto.getStartTime(), dto.getEndTime()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(UUID.randomUUID());
            return reservation;
        });

        ReservationResponseDTO response = reservationService.createReservation(condominiumId, dto);

        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals("Salão de festas", response.getArea());
        assertEquals("Maria Souza", response.getRequestedByName());
        assertEquals("201", response.getRequestedByUnit());
        assertEquals(ReservationStatus.PENDING, response.getStatus());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservationRejectsConflictingReservation() {
        ReservationCreateDTO dto = ReservationCreateDTO.builder()
                .area("Salão de festas")
                .unitNumber("201")
                .startTime(LocalDateTime.of(2026, 6, 9, 18, 0))
                .endTime(LocalDateTime.of(2026, 6, 9, 22, 0))
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(requester));
        when(reservationRepository.findConflictingReservations(eq(condominiumId), eq(dto.getArea()), any(), any()))
                .thenReturn(List.of(Reservation.builder().id(UUID.randomUUID()).build()));

        assertThrows(com.api.sindigo.exception.ValidationException.class, () -> reservationService.createReservation(condominiumId, dto));
    }

    @Test
    void listByCondominiumForOwnerReturnsAllReservations() {
        Pageable pageable = PageRequest.of(0, 10);
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .requestedBy(requester)
                .requestedByName(requester.getName())
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(reservationRepository.findByCondominiumIdOrderByStartTimeDesc(condominiumId, pageable))
                .thenReturn(new PageImpl<>(List.of(reservation), pageable, 1));

        Page<ReservationResponseDTO> page = reservationService.listByCondominium(condominiumId, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("Maria Souza", page.getContent().getFirst().getRequestedByName());
    }

    @Test
    void listByCondominiumForMemberReturnsOnlyOwnReservations() {
        UUID memberId = UUID.randomUUID();
        condominium.setOwner(User.builder().id(UUID.randomUUID()).build());
        Pageable pageable = PageRequest.of(0, 10);
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .requestedBy(User.builder().id(memberId).name("Maria Souza").build())
                .requestedByName("Maria Souza")
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(memberId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, memberId)).thenReturn(Optional.of(condominium));
        when(reservationRepository.findByCondominiumIdAndStatusAndRequestedByIdOrderByStartTimeDesc(eq(condominiumId), any(), eq(memberId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation), pageable, 1));

        Page<ReservationResponseDTO> page = reservationService.listByCondominiumAndStatus(condominiumId, ReservationStatus.PENDING, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("Maria Souza", page.getContent().getFirst().getRequestedByName());
    }

    @Test
    void checkAvailabilityReturnsUnavailableWhenConflictsExist() {
        LocalDate date = LocalDate.of(2026, 6, 1);
        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(reservationRepository.findConflictingReservations(eq(condominiumId), eq("Salão de festas"), any(), any()))
                .thenReturn(List.of(Reservation.builder().id(UUID.randomUUID()).build()));

        var result = reservationService.checkAvailability(condominiumId, "Salão de festas", date);

        assertEquals(condominiumId, result.get("condominiumId"));
        assertEquals(false, result.get("available"));
        assertEquals(1, result.get("conflictsFound"));
    }

    @Test
    void approveReservationConfirmsPendingReservation() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .condominium(condominium)
                .status(ReservationStatus.PENDING)
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(reservationRepository.findByIdAndCondominiumId(reservationId, condominiumId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponseDTO response = reservationService.approveReservation(
                condominiumId,
                reservationId,
                new ReservationApprovalDTO(ReservationStatus.CONFIRMED, "ok")
        );

        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void approveReservationRejectsNonPendingReservations() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .condominium(condominium)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(reservationRepository.findByIdAndCondominiumId(reservationId, condominiumId)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class, () -> reservationService.approveReservation(
                condominiumId,
                reservationId,
                new ReservationApprovalDTO(ReservationStatus.CONFIRMED, null)
        ));
    }
}



