package com.api.sindigo.core.ticket.dto;

import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {

    private UUID id;
    private String title;
    private String description;
    private String location;
    private TicketStatus status;
    private TicketCategory category;
    private TicketPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    private UUID condominiumId;
    private UUID createdById;
    private UUID assignedToId;

    private List<UUID> activityIds;

    private String notes;
    private String estimatedResolution;
}

