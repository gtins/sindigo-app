package com.api.sindigo.core.reservation;

import com.api.sindigo.core.reservation.entities.Reservation;
import com.api.sindigo.core.reservation.entities.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Page<Reservation> findByCondominiumIdAndStatusOrderByStartTimeDesc(
            UUID condominiumId,
            ReservationStatus status,
            Pageable pageable
    );

    Page<Reservation> findByCondominiumIdOrderByStartTimeDesc(
            UUID condominiumId,
            Pageable pageable
    );

    @Query("SELECT r FROM Reservation r WHERE r.condominium.id = :condominiumId " +
           "AND r.area = :area " +
           "AND r.status != 'CANCELLED' " +
           "AND r.startTime < :endTime " +
           "AND r.endTime > :startTime")
    List<Reservation> findConflictingReservations(
            @Param("condominiumId") UUID condominiumId,
            @Param("area") String area,
            @Param("startTime") @NotNull(message = "Start time is required") LocalDateTime startTime,
            @Param("endTime") @NotNull(message = "End time is required") LocalDateTime endTime
    );
}
