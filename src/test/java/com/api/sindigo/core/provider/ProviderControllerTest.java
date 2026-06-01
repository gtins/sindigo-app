package com.api.sindigo.core.provider;

import com.api.sindigo.core.provider.dto.ProviderCreateDTO;
import com.api.sindigo.core.provider.dto.ProviderResponseDTO;
import com.api.sindigo.core.provider.dto.ProviderUpdateDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProviderControllerTest {

    private final ProviderService providerService = mock(ProviderService.class);
    private final ProviderController controller = new ProviderController(providerService);

    @Test
    void createProviderReturnsCreatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        ProviderCreateDTO dto = new ProviderCreateDTO(
                "Carlos Manutenção",
                "Elétrica",
                "11999999999",
                "carlos@example.com",
                "Atendimento noturno"
        );
        ProviderResponseDTO response = buildProviderResponse(condominiumId);
        response.setName(dto.getName());
        response.setServiceType(dto.getServiceType());
        response.setPhone(dto.getPhone());
        response.setEmail(dto.getEmail());
        response.setNotes(dto.getNotes());

        when(providerService.createProvider(condominiumId, dto)).thenReturn(response);

        var result = controller.createProvider(condominiumId, dto);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(providerService).createProvider(condominiumId, dto);
    }

    @Test
    void getProvidersReturnsList() {
        UUID condominiumId = UUID.randomUUID();
        ProviderResponseDTO response = buildProviderResponse(condominiumId);
        when(providerService.getProvidersByCondominium(condominiumId)).thenReturn(List.of(response));

        var result = controller.getProviders(condominiumId);

        assertEquals(200, result.getStatusCode().value());
        List<ProviderResponseDTO> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(response, body.getFirst());
    }

    @Test
    void getProviderByIdReturnsResponse() {
        UUID condominiumId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        ProviderResponseDTO response = buildProviderResponse(condominiumId);
        response.setId(providerId);
        when(providerService.getProviderById(condominiumId, providerId)).thenReturn(response);

        var result = controller.getProviderById(condominiumId, providerId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void getProviderByIdReturnsResponseBodyNotNull() {
        UUID condominiumId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        ProviderResponseDTO response = buildProviderResponse(condominiumId);
        response.setId(providerId);
        when(providerService.getProviderById(condominiumId, providerId)).thenReturn(response);

        var result = controller.getProviderById(condominiumId, providerId);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    void updateProviderReturnsUpdatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        ProviderUpdateDTO dto = new ProviderUpdateDTO(
                "Carlos Atualizado",
                "Hidráulica",
                "11888888888",
                "carlos.novo@example.com",
                "Novo note"
        );
        ProviderResponseDTO response = buildProviderResponse(condominiumId);
        response.setId(providerId);
        response.setName(dto.getName());
        response.setServiceType(dto.getServiceType());
        response.setPhone(dto.getPhone());
        response.setEmail(dto.getEmail());
        response.setNotes(dto.getNotes());

        when(providerService.updateProvider(eq(condominiumId), eq(providerId), eq(dto))).thenReturn(response);

        var result = controller.updateProvider(condominiumId, providerId, dto);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteProviderReturnsNoContent() {
        UUID condominiumId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();

        var result = controller.deleteProvider(condominiumId, providerId);

        assertEquals(204, result.getStatusCode().value());
        verify(providerService).deleteProvider(condominiumId, providerId);
    }

    private ProviderResponseDTO buildProviderResponse(UUID condominiumId) {
        ProviderResponseDTO dto = new ProviderResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setName("Carlos Manutenção");
        dto.setServiceType("Elétrica");
        dto.setPhone("11999999999");
        dto.setEmail("carlos@example.com");
        dto.setNotes("Atendimento noturno");
        dto.setCreatedAt(LocalDate.of(2026, 6, 1));
        dto.setUpdatedAt(LocalDate.of(2026, 6, 1));
        dto.setCondominiumId(condominiumId);
        dto.setActivityIds(List.of(UUID.randomUUID()));
        return dto;
    }
}

