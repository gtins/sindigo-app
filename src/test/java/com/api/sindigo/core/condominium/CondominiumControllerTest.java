package com.api.sindigo.core.condominium;

import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CondominiumControllerTest {

    private final CondominiumService condominiumService = mock(CondominiumService.class);
    private final CondominiumController controller = new CondominiumController(condominiumService);

    @Test
    void createReturnsCreatedResponse() {
        CondominiumCreateDTO dto = CondominiumCreateDTO.builder()
                .name("Residencial Alfa")
                .address("Rua A, 123")
                .unidades(12)
                .build();
        CondominiumResponseDTO response = buildResponse();
        response.setName(dto.getName());
        response.setAddress(dto.getAddress());
        response.setUnidades(dto.getUnidades());

        when(condominiumService.createCondominium(dto)).thenReturn(response);

        var result = controller.create(dto);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(condominiumService).createCondominium(dto);
    }

    @Test
    void listReturnsCondominiums() {
        CondominiumResponseDTO response = buildResponse();
        when(condominiumService.listCondominiums()).thenReturn(List.of(response));

        var result = controller.list();

        assertEquals(200, result.getStatusCode().value());
        List<CondominiumResponseDTO> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(response, body.getFirst());
    }

    @Test
    void getByIdReturnsCondominium() {
        UUID condominiumId = UUID.randomUUID();
        CondominiumResponseDTO response = buildResponse();
        response.setId(condominiumId);
        when(condominiumService.getById(condominiumId)).thenReturn(response);

        var result = controller.getById(condominiumId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    private CondominiumResponseDTO buildResponse() {
        return CondominiumResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("Residencial Alfa")
                .address("Rua A, 123")
                .unidades(12)
                .createdAt(LocalDate.of(2026, 6, 1))
                .build();
    }
}

