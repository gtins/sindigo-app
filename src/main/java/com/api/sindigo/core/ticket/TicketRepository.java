package com.api.sindigo.core.ticket;

import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByCondominiumIdOrderByCreatedAtDesc(UUID condominiumId);

    List<Ticket> findByCondominiumIdAndStatus(UUID condominiumId, TicketStatus status);

    Optional<Ticket> findByIdAndCondominiumId(UUID id, UUID condominiumId);

    @Query("SELECT t FROM Ticket t WHERE t.condominium.id = :condominiumId AND t.status = :status ORDER BY t.createdAt DESC")
    List<Ticket> findOpenTickets(@Param("condominiumId") UUID condominiumId, @Param("status") TicketStatus status);
}

