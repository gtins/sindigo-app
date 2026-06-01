package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCloseDTO;
import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.ActivityOrigin;
import com.api.sindigo.core.activity.entities.ActivityStatus;
import com.api.sindigo.core.activity.entities.ActivityType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivityControllerTest {

    private final ActivityService activityService = mock(ActivityService.class);
    private final ActivityController controller = new ActivityController(activityService);

    @Test
    void createActivityReturnsCreatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        ActivityCreateDTO dto = new ActivityCreateDTO(
                "Limpeza da caixa d'água",
                "Limpeza preventiva",
                ActivityType.PERIODIC,
                ActivityOrigin.PLANEJAMENTO,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 2),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        ActivityResponseDTO response = buildResponse(condominiumId, ActivityStatus.PENDING);

        when(activityService.addActivity(condominiumId, dto)).thenReturn(response);

        var result = controller.createActivity(condominiumId, dto);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(activityService).addActivity(condominiumId, dto);
    }

    @Test
    void listReturnsActivities() {
        UUID condominiumId = UUID.randomUUID();
        ActivityResponseDTO response = buildResponse(condominiumId, ActivityStatus.PENDING);
        when(activityService.listByCondominium(condominiumId)).thenReturn(List.of(response));

        var result = controller.list(condominiumId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
    }

    @Test
    void closeActivityReturnsUpdatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        ActivityCloseDTO dto = new ActivityCloseDTO(ActivityStatus.COMPLETED, "Finalizada com sucesso");
        ActivityResponseDTO response = buildResponse(condominiumId, ActivityStatus.COMPLETED);
        response.setId(activityId);
        response.setClosedAt(LocalDateTime.of(2026, 6, 1, 10, 0));

        when(activityService.closeActivity(eq(condominiumId), eq(activityId), eq(dto))).thenReturn(response);

        var result = controller.closeActivity(condominiumId, activityId, dto);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    private ActivityResponseDTO buildResponse(UUID condominiumId, ActivityStatus status) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setCondominiumId(condominiumId);
        dto.setTitle("Limpeza da caixa d'água");
        dto.setDescription("Limpeza preventiva");
        dto.setType(ActivityType.PERIODIC);
        dto.setOrigin(ActivityOrigin.PLANEJAMENTO);
        dto.setStatus(status);
        dto.setStartDate(LocalDate.of(2026, 6, 1));
        dto.setEndDate(LocalDate.of(2026, 6, 2));
        dto.setCreatedAt(LocalDate.of(2026, 6, 1));
        dto.setUpdatedAt(LocalDate.of(2026, 6, 1));
        dto.setCreatedById(UUID.randomUUID());
        dto.setTicketId(UUID.randomUUID());
        dto.setProviderId(UUID.randomUUID());
        return dto;
    }
}

