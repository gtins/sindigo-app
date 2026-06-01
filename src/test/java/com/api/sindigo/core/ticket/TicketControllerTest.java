package com.api.sindigo.core.ticket;

import com.api.sindigo.core.ticket.dto.TicketCloseDTO;
import com.api.sindigo.core.ticket.dto.TicketCreateDTO;
import com.api.sindigo.core.ticket.dto.TicketResponseDTO;
import com.api.sindigo.core.ticket.dto.TicketUpdateDTO;
import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketControllerTest {

    private final TicketService ticketService = mock(TicketService.class);
    private final TicketController controller = new TicketController(ticketService);

    @Test
    void createTicketReturnsCreatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        TicketCreateDTO dto = new TicketCreateDTO(
                "Vazamento",
                "Vazamento no banheiro social",
                "Bloco A",
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                "Primeira abertura"
        );
        TicketResponseDTO response = buildTicketResponse(condominiumId, TicketStatus.ABERTO);

        when(ticketService.createTicket(condominiumId, dto)).thenReturn(response);

        var result = controller.createTicket(condominiumId, dto);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(ticketService).createTicket(condominiumId, dto);
    }

    @Test
    void getTicketsReturnsList() {
        UUID condominiumId = UUID.randomUUID();
        TicketResponseDTO response = buildTicketResponse(condominiumId, TicketStatus.ABERTO);
        when(ticketService.getTicketsByCondominium(condominiumId)).thenReturn(List.of(response));

        var result = controller.getTickets(condominiumId);

        assertEquals(200, result.getStatusCode().value());
        List<TicketResponseDTO> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(response, body.getFirst());
    }

    @Test
    void getTicketByIdReturnsResponse() {
        UUID condominiumId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        TicketResponseDTO response = buildTicketResponse(condominiumId, TicketStatus.ABERTO);
        response.setId(ticketId);
        when(ticketService.getTicketById(condominiumId, ticketId)).thenReturn(response);

        var result = controller.getTicketById(condominiumId, ticketId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateTicketReturnsUpdatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        TicketUpdateDTO dto = new TicketUpdateDTO(
                "Vazamento resolvido",
                "Problema corrigido",
                "Bloco A",
                TicketStatus.RESOLVIDO,
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                "Atualizado"
        );
        TicketResponseDTO response = buildTicketResponse(condominiumId, TicketStatus.RESOLVIDO);
        response.setId(ticketId);
        when(ticketService.updateTicket(condominiumId, ticketId, dto)).thenReturn(response);

        var result = controller.updateTicket(condominiumId, ticketId, dto);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteTicketReturnsNoContent() {
        UUID condominiumId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        var result = controller.deleteTicket(condominiumId, ticketId);

        assertEquals(204, result.getStatusCode().value());
        verify(ticketService).deleteTicket(condominiumId, ticketId);
    }

    @Test
    void closeTicketReturnsClosedResponse() {
        UUID condominiumId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        TicketCloseDTO dto = new TicketCloseDTO(TicketStatus.FECHADO, "Conserto concluído");
        TicketResponseDTO response = buildTicketResponse(condominiumId, TicketStatus.FECHADO);
        response.setId(ticketId);
        response.setNotes("[01/06/2026 12:00:00] Conserto concluído");
        when(ticketService.closeTicket(eq(condominiumId), eq(ticketId), eq(dto))).thenReturn(response);

        var result = controller.closeTicket(condominiumId, ticketId, dto);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    private TicketResponseDTO buildTicketResponse(UUID condominiumId, TicketStatus status) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setTitle("Vazamento");
        dto.setDescription("Vazamento no banheiro social");
        dto.setLocation("Bloco A");
        dto.setStatus(status);
        dto.setCategory(TicketCategory.MANUTENCAO);
        dto.setPriority(TicketPriority.ALTA);
        dto.setCreatedAt(LocalDateTime.of(2026, 6, 1, 8, 0));
        dto.setUpdatedAt(LocalDateTime.of(2026, 6, 1, 9, 0));
        dto.setClosedAt(status == TicketStatus.ABERTO ? null : LocalDateTime.of(2026, 6, 1, 10, 0));
        dto.setCondominiumId(condominiumId);
        dto.setCreatedById(UUID.randomUUID());
        dto.setAssignedToId(UUID.randomUUID());
        dto.setActivityIds(List.of(UUID.randomUUID()));
        dto.setNotes("Primeira abertura");
        dto.setEstimatedResolution("24h");
        return dto;
    }
}



