package com.api.sindigo.core.reservation;

import com.api.sindigo.core.reservation.entities.Reservation;
import com.api.sindigo.core.reservation.entities.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    // Listar reservas por condomínio com status filtrado e paginação
    Page<Reservation> findByCondominiumIdAndStatusOrderByStartTimeDesc(
            UUID condominiumId,
            ReservationStatus status,
            Pageable pageable
    );

    // Listar todas as reservas do condomínio ordenadas por data
    Page<Reservation> findByCondominiumIdOrderByStartTimeDesc(
            UUID condominiumId,
            Pageable pageable
    );

    // Detectar conflito de horário: reservas que se sobrepõem no mesmo horário e área
    @Query("SELECT r FROM Reservation r WHERE r.condominium.id = :condominiumId " +
           "AND r.area = :area " +
           "AND r.status != 'CANCELLED' " +
           "AND r.startTime < :endTime " +
           "AND r.endTime > :startTime")
    List<Reservation> findConflictingReservations(
            @Param("condominiumId") UUID condominiumId,
            @Param("area") String area,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );
}
