package com.api.sindigo.core.ticket;

import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.ticket.dto.TicketCloseDTO;
import com.api.sindigo.core.ticket.dto.TicketCreateDTO;
import com.api.sindigo.core.ticket.dto.TicketResponseDTO;
import com.api.sindigo.core.ticket.dto.TicketUpdateDTO;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import com.api.sindigo.core.ticket.validator.TicketValidator;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final String CONDOMINIUM_ACCESS_ERROR_MESSAGE =
            "Condominium not found or you don't have access";

    private static final String TICKET_NOT_FOUND_MESSAGE = "Ticket not found";
    private static final String AUTHENTICATED_USER_NOT_FOUND_MESSAGE = "Authenticated user not found";

    private static final String INVALID_CLOSING_STATUS_MESSAGE =
            "Invalid closing status. Must be RESOLVIDO, FECHADO, or CANCELADO";

    private static final DateTimeFormatter CLOSING_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final TicketRepository ticketRepository;
    private final CondominiumRepository condominiumRepository;
    private final TicketValidator ticketValidator;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @Transactional
    public TicketResponseDTO createTicket(UUID condominiumId, TicketCreateDTO dto) {
        ticketValidator.validateTicketCreation(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        Condominium condominium = condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        User creator = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException(AUTHENTICATED_USER_NOT_FOUND_MESSAGE));

        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setLocation(dto.getLocation());
        ticket.setCategory(dto.getCategory());
        ticket.setPriority(dto.getPriority());
        ticket.setStatus(TicketStatus.ABERTO);
        ticket.setCondominium(condominium);
        ticket.setCreatedBy(creator);
        ticket.setNotes(dto.getNotes());
        ticket.setEstimatedResolution(dto.getEstimatedResolution());

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToDTO(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByCondominium(UUID condominiumId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        return ticketRepository.findByCondominiumIdOrderByCreatedAtDesc(condominiumId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(UUID condominiumId, UUID ticketId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Ticket ticket = ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(TICKET_NOT_FOUND_MESSAGE));

        return mapToDTO(ticket);
    }

    @Transactional
    public TicketResponseDTO updateTicket(UUID condominiumId, UUID ticketId, TicketUpdateDTO dto) {
        ticketValidator.validateTicketUpdate(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Ticket ticket = ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(TICKET_NOT_FOUND_MESSAGE));

        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setLocation(dto.getLocation());
        ticket.setStatus(dto.getStatus());
        ticket.setCategory(dto.getCategory());
        ticket.setPriority(dto.getPriority());
        ticket.setNotes(dto.getNotes());
        ticket.setEstimatedResolution(dto.getEstimatedResolution());

        if (dto.getStatus() == TicketStatus.FECHADO || dto.getStatus() == TicketStatus.RESOLVIDO) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToDTO(updatedTicket);
    }

    @Transactional
    public void deleteTicket(UUID condominiumId, UUID ticketId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Ticket ticket = ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(TICKET_NOT_FOUND_MESSAGE));

        ticketRepository.delete(ticket);
    }

    @Transactional
    public TicketResponseDTO closeTicket(UUID condominiumId, UUID ticketId, TicketCloseDTO dto) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Ticket ticket = ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(TICKET_NOT_FOUND_MESSAGE));

        if (!isValidClosingStatus(dto.getStatus())) {
            throw new IllegalArgumentException(INVALID_CLOSING_STATUS_MESSAGE);
        }

        ticket.setStatus(dto.getStatus());
        ticket.setClosedAt(LocalDateTime.now());

        if (dto.getClosingNotes() != null && !dto.getClosingNotes().isBlank()) {
            String existingNotes = ticket.getNotes() != null ? ticket.getNotes() : "";
            String timestamp = LocalDateTime.now().format(CLOSING_DATE_FORMATTER);

            ticket.setNotes(
                    existingNotes
                            + (existingNotes.isEmpty() ? "" : "\n\n")
                            + "["
                            + timestamp
                            + "] "
                            + dto.getClosingNotes()
            );
        }

        Ticket closedTicket = ticketRepository.save(ticket);
        return mapToDTO(closedTicket);
    }

    private boolean isValidClosingStatus(TicketStatus status) {
        return status == TicketStatus.RESOLVIDO
                || status == TicketStatus.FECHADO
                || status == TicketStatus.CANCELADO;
    }

    private TicketResponseDTO mapToDTO(Ticket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setLocation(ticket.getLocation());
        dto.setStatus(ticket.getStatus());
        dto.setCategory(ticket.getCategory());
        dto.setPriority(ticket.getPriority());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setClosedAt(ticket.getClosedAt());
        dto.setCondominiumId(ticket.getCondominium().getId());
        dto.setCreatedById(ticket.getCreatedBy().getId());

        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToId(ticket.getAssignedTo().getId());
        }

        dto.setNotes(ticket.getNotes());
        dto.setEstimatedResolution(ticket.getEstimatedResolution());
        dto.setActivityIds(ticket.getActivities()
                .stream()
                .map(Activity::getId)
                .toList());

        return dto;
    }
}