package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.activity.entities.ActivityOrigin;
import com.api.sindigo.core.activity.entities.ActivityStatus;
import com.api.sindigo.core.activity.entities.ActivityType;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActivityDtoMapperTest {

    @Test
    void toResponseDTOMapsActivityWithAllFields() {
        UUID activityId = UUID.randomUUID();
        UUID condominiumId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 2);
        LocalDate createdAt = LocalDate.of(2026, 6, 1);
        LocalDate updatedAt = LocalDate.of(2026, 6, 1);
        LocalDateTime closedAt = LocalDateTime.of(2026, 6, 2, 10, 0);

        Condominium condominium = Condominium.builder().id(condominiumId).build();
        User createdBy = User.builder().id(createdById).build();
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .title("Chamado")
                .description("Descrição")
                .location("Local")
                .status(TicketStatus.ABERTO)
                .category(TicketCategory.MANUTENCAO)
                .priority(TicketPriority.ALTA)
                .condominium(condominium)
                .createdBy(createdBy)
                .activities(Collections.emptyList())
                .build();
        Provider provider = Provider.builder()
                .id(providerId)
                .condominium(condominium)
                .activities(Collections.emptyList())
                .build();

        Activity activity = Activity.builder()
                .id(activityId)
                .title("Limpeza da caixa d'água")
                .description("Limpeza preventiva")
                .type(ActivityType.PERIODIC)
                .origin(ActivityOrigin.PLANEJAMENTO)
                .status(ActivityStatus.PENDING)
                .startDate(startDate)
                .endDate(endDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .closedAt(closedAt)
                .condominium(condominium)
                .createdBy(createdBy)
                .ticket(ticket)
                .provider(provider)
                .instances(Collections.emptyList())
                .build();

        ActivityResponseDTO dto = ActivityDtoMapper.toResponseDTO(activity);

        assertNotNull(dto);
        assertEquals(activityId, dto.getId());
        assertEquals(condominiumId, dto.getCondominiumId());
        assertEquals("Limpeza da caixa d'água", dto.getTitle());
        assertEquals("Limpeza preventiva", dto.getDescription());
        assertEquals(ActivityType.PERIODIC, dto.getType());
        assertEquals(ActivityOrigin.PLANEJAMENTO, dto.getOrigin());
        assertEquals(ActivityStatus.PENDING, dto.getStatus());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
        assertEquals(closedAt, dto.getClosedAt());
        assertEquals(createdById, dto.getCreatedById());
        assertEquals(ticketId, dto.getTicketId());
        assertEquals(providerId, dto.getProviderId());
    }

    @Test
    void toResponseDTOMapsActivityWithoutTicket() {
        UUID activityId = UUID.randomUUID();
        UUID condominiumId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        Condominium condominium = Condominium.builder().id(condominiumId).build();
        User createdBy = User.builder().id(createdById).build();

        Activity activity = Activity.builder()
                .id(activityId)
                .title("Atividade Simples")
                .description("Sem relacionamento com ticket")
                .type(ActivityType.ONCE)
                .origin(ActivityOrigin.MANUAL)
                .status(ActivityStatus.COMPLETED)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 1))
                .createdAt(LocalDate.of(2026, 6, 1))
                .updatedAt(LocalDate.of(2026, 6, 1))
                .condominium(condominium)
                .createdBy(createdBy)
                .ticket(null)
                .provider(null)
                .instances(Collections.emptyList())
                .build();

        ActivityResponseDTO dto = ActivityDtoMapper.toResponseDTO(activity);

        assertNotNull(dto);
        assertEquals(activityId, dto.getId());
        assertNull(dto.getTicketId());
        assertNull(dto.getProviderId());
        assertEquals("Atividade Simples", dto.getTitle());
    }

    @Test
    void toResponseDTOMapsActivityWithProvider() {
        UUID activityId = UUID.randomUUID();
        UUID condominiumId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();

        Condominium condominium = Condominium.builder().id(condominiumId).build();
        User createdBy = User.builder().id(createdById).build();
        Provider provider = Provider.builder()
                .id(providerId)
                .condominium(condominium)
                .activities(Collections.emptyList())
                .build();

        Activity activity = Activity.builder()
                .id(activityId)
                .title("Serviço do Fornecedor")
                .description("Atividade com fornecedor")
                .type(ActivityType.PERIODIC)
                .origin(ActivityOrigin.PLANEJAMENTO)
                .status(ActivityStatus.PENDING)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 5))
                .createdAt(LocalDate.of(2026, 6, 1))
                .updatedAt(LocalDate.of(2026, 6, 1))
                .condominium(condominium)
                .createdBy(createdBy)
                .ticket(null)
                .provider(provider)
                .instances(Collections.emptyList())
                .build();

        ActivityResponseDTO dto = ActivityDtoMapper.toResponseDTO(activity);

        assertNotNull(dto);
        assertEquals(providerId, dto.getProviderId());
        assertNull(dto.getTicketId());
    }

    @Test
    void toResponseDTOMapsActivityWithCompletedStatus() {
        UUID condominiumId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        LocalDateTime closedAt = LocalDateTime.of(2026, 6, 5, 14, 30);

        Condominium condominium = Condominium.builder().id(condominiumId).build();
        User createdBy = User.builder().id(createdById).build();

        Activity activity = Activity.builder()
                .id(UUID.randomUUID())
                .title("Atividade Finalizada")
                .description("Descri ção")
                .type(ActivityType.ONCE)
                .origin(ActivityOrigin.MANUAL)
                .status(ActivityStatus.COMPLETED)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 5))
                .createdAt(LocalDate.of(2026, 6, 1))
                .updatedAt(LocalDate.of(2026, 6, 5))
                .closedAt(closedAt)
                .condominium(condominium)
                .createdBy(createdBy)
                .ticket(null)
                .provider(null)
                .instances(Collections.emptyList())
                .build();

        ActivityResponseDTO dto = ActivityDtoMapper.toResponseDTO(activity);

        assertNotNull(dto);
        assertEquals(ActivityStatus.COMPLETED, dto.getStatus());
        assertEquals(closedAt, dto.getClosedAt());
    }
}

