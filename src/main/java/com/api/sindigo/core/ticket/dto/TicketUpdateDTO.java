package com.api.sindigo.core.ticket.dto;

import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpdateDTO {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    private String location;

    @NotNull(message = "Status é obrigatório")
    private TicketStatus status;

    @NotNull(message = "Categoria é obrigatória")
    private TicketCategory category;

    @NotNull(message = "Prioridade é obrigatória")
    private TicketPriority priority;

    private String estimatedResolution;
    private String notes;
}

