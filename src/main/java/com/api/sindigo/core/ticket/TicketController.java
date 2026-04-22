package com.api.sindigo.core.ticket;

import com.api.sindigo.core.ticket.dto.TicketCreateDTO;
import com.api.sindigo.core.ticket.dto.TicketResponseDTO;
import com.api.sindigo.core.ticket.dto.TicketUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/condominiums/{condominiumId}/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(
            @PathVariable UUID condominiumId,
            @Valid @RequestBody TicketCreateDTO dto) {
        TicketResponseDTO response = ticketService.createTicket(condominiumId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getTickets(@PathVariable UUID condominiumId) {
        List<TicketResponseDTO> response = ticketService.getTicketsByCondominium(condominiumId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> getTicketById(
            @PathVariable UUID condominiumId,
            @PathVariable UUID ticketId) {
        TicketResponseDTO response = ticketService.getTicketById(condominiumId, ticketId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable UUID condominiumId,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketUpdateDTO dto) {
        TicketResponseDTO response = ticketService.updateTicket(condominiumId, ticketId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable UUID condominiumId,
            @PathVariable UUID ticketId) {
        ticketService.deleteTicket(condominiumId, ticketId);
        return ResponseEntity.noContent().build();
    }
}

