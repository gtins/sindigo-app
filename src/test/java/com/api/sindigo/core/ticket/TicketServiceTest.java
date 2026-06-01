package com.api.sindigo.core.ticket;

import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.ticket.dto.TicketCloseDTO;
import com.api.sindigo.core.ticket.dto.TicketCreateDTO;
import com.api.sindigo.core.ticket.dto.TicketResponseDTO;
import com.api.sindigo.core.ticket.dto.TicketUpdateDTO;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import com.api.sindigo.core.ticket.validator.TicketValidator;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TicketServiceTest {

    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final CondominiumRepository condominiumRepository = mock(CondominiumRepository.class);
    private final TicketValidator ticketValidator = new TicketValidator();
    private final SecurityContextHelper securityContextHelper = mock(SecurityContextHelper.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TicketService ticketService = new TicketService(
            ticketRepository,
            condominiumRepository,
            ticketValidator,
            securityContextHelper,
            userRepository
    );

    private UUID authenticatedUserId;
    private UUID condominiumId;
    private Condominium condominium;
    private User creator;

    @BeforeEach
    void setUp() {
        authenticatedUserId = UUID.randomUUID();
        condominiumId = UUID.randomUUID();
        creator = User.builder()
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
    void createTicketPersistsOpenedTicket() {
        TicketCreateDTO dto = new TicketCreateDTO(
                "Vazamento",
                "Vazamento no banheiro social",
                "Bloco A",
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                "Primeira abertura"
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(creator));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(UUID.randomUUID());
            return ticket;
        });

        TicketResponseDTO response = ticketService.createTicket(condominiumId, dto);

        assertEquals("Vazamento", response.getTitle());
        assertEquals(TicketStatus.ABERTO, response.getStatus());
        assertEquals(condominiumId, response.getCondominiumId());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicketRejectsWhenCondominiumIsNotAccessible() {
        TicketCreateDTO dto = new TicketCreateDTO(
                "Vazamento",
                "Vazamento no banheiro social",
                "Bloco A",
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                "Primeira abertura"
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.createTicket(condominiumId, dto));
        verifyNoInteractions(ticketRepository, userRepository);
    }

    @Test
    void getTicketsByCondominiumMapsList() {
        Ticket ticket = buildTicket(TicketStatus.ABERTO);
        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(ticketRepository.findByCondominiumIdOrderByCreatedAtDesc(condominiumId)).thenReturn(List.of(ticket));

        List<TicketResponseDTO> result = ticketService.getTicketsByCondominium(condominiumId);

        assertEquals(1, result.size());
        assertEquals(ticket.getId(), result.getFirst().getId());
        assertEquals("Vazamento", result.getFirst().getTitle());
    }

    @Test
    void getTicketByIdReturnsTicket() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(TicketStatus.ABERTO);
        ticket.setId(ticketId);

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)).thenReturn(Optional.of(ticket));

        TicketResponseDTO result = ticketService.getTicketById(condominiumId, ticketId);

        assertEquals(ticketId, result.getId());
        assertEquals("Vazamento", result.getTitle());
    }

    @Test
    void updateTicketSetsClosedAtWhenStatusClosesTicket() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(TicketStatus.ABERTO);
        ticket.setId(ticketId);
        TicketUpdateDTO dto = new TicketUpdateDTO(
                "Vazamento resolvido",
                "Problema corrigido",
                "Bloco A",
                TicketStatus.FECHADO,
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                "Atualizado"
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponseDTO result = ticketService.updateTicket(condominiumId, ticketId, dto);

        assertEquals(TicketStatus.FECHADO, result.getStatus());
        assertNotNull(result.getClosedAt());
        assertEquals("Atualizado", result.getNotes());
    }

    @Test
    void updateTicketKeepsClosedAtNullWhenStatusRemainsOpen() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(TicketStatus.ABERTO);
        ticket.setId(ticketId);
        TicketUpdateDTO dto = new TicketUpdateDTO(
                "Vazamento em análise",
                "Problema em análise",
                "Bloco A",
                TicketStatus.EM_ANALISE,
                TicketCategory.MANUTENCAO,
                TicketPriority.ALTA,
                "24h",
                "Sem fechamento"
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponseDTO result = ticketService.updateTicket(condominiumId, ticketId, dto);

        assertEquals(TicketStatus.EM_ANALISE, result.getStatus());
        assertNull(result.getClosedAt());
    }

    @Test
    void deleteTicketDeletesExistingTicket() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(TicketStatus.ABERTO);
        ticket.setId(ticketId);

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(condominiumId, ticketId);

        verify(ticketRepository).delete(ticket);
    }

    @Test
    void closeTicketUpdatesNotesAndStatus() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(TicketStatus.EM_ANALISE);
        ticket.setId(ticketId);
        ticket.setNotes("Nota anterior");
        TicketCloseDTO dto = new TicketCloseDTO(TicketStatus.FECHADO, "Conserto finalizado");

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponseDTO result = ticketService.closeTicket(condominiumId, ticketId, dto);

        assertEquals(TicketStatus.FECHADO, result.getStatus());
        assertNotNull(result.getClosedAt());
        assertTrue(result.getNotes().startsWith("Nota anterior\n\n["));
    }

    @Test
    void closeTicketRejectsInvalidStatus() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(TicketStatus.EM_ANALISE);
        ticket.setId(ticketId);
        TicketCloseDTO dto = new TicketCloseDTO(TicketStatus.ABERTO, "Inválido");

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)).thenReturn(Optional.of(ticket));

        assertThrows(IllegalArgumentException.class, () -> ticketService.closeTicket(condominiumId, ticketId, dto));
    }

    private Ticket buildTicket(TicketStatus status) {
        return Ticket.builder()
                .id(UUID.randomUUID())
                .title("Vazamento")
                .description("Vazamento no banheiro social")
                .location("Bloco A")
                .status(status)
                .category(TicketCategory.MANUTENCAO)
                .priority(TicketPriority.ALTA)
                .condominium(condominium)
                .createdBy(creator)
                .assignedTo(null)
                .activities(Collections.emptyList())
                .notes("Nota anterior")
                .estimatedResolution("24h")
                .createdAt(LocalDateTime.of(2026, 6, 1, 8, 0))
                .updatedAt(LocalDateTime.of(2026, 6, 1, 9, 0))
                .closedAt(status == TicketStatus.ABERTO ? null : LocalDateTime.of(2026, 6, 1, 10, 0))
                .build();
    }
}

