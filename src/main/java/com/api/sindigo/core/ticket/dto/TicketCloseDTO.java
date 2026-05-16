package com.api.sindigo.core.ticket.dto;

import com.api.sindigo.core.ticket.entities.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCloseDTO {

    @NotNull(message = "Status is required")
    private TicketStatus status;  // RESOLVIDO, FECHADO, ou CANCELADO

    private String closingNotes;  // Notas de encerramento
}

