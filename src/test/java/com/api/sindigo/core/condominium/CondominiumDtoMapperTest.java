package com.api.sindigo.core.condominium;

import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.entities.Condominium;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CondominiumDtoMapperTest {

    private final CondominiumDtoMapper mapper = new CondominiumDtoMapper();

    @Test
    void toDomainMapsDTOToEntity() {
        String name = "Condomínio Alfa";
        String address = "Rua das Flores, 123";
        Integer unidades = 15;

        CondominiumCreateDTO dto = new CondominiumCreateDTO(name, address, unidades);

        Condominium domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(name, domain.getName());
        assertEquals(address, domain.getAddress());
        assertEquals(unidades, domain.getUnidades());
    }

    @Test
    void toResponseMapsDomainToDTO() {
        UUID condominiumId = UUID.randomUUID();
        String name = "Residencial Beta";
        String address = "Avenida Principal, 456";
        Integer unidades = 20;
        LocalDate createdAt = LocalDate.of(2026, 1, 15);

        Condominium domain = Condominium.builder()
                .id(condominiumId)
                .name(name)
                .address(address)
                .unidades(unidades)
                .createdAt(createdAt)
                .build();

        CondominiumResponseDTO response = mapper.toResponse(domain);

        assertNotNull(response);
        assertEquals(condominiumId, response.getId());
        assertEquals(name, response.getName());
        assertEquals(address, response.getAddress());
        assertEquals(unidades, response.getUnidades());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void toDomainWithMinimalUnidades() {
        CondominiumCreateDTO dto = new CondominiumCreateDTO("Condomínio", "Endereço", 1);

        Condominium domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(1, domain.getUnidades());
    }

    @Test
    void toDomainWithMaximalUnidades() {
        CondominiumCreateDTO dto = new CondominiumCreateDTO("Condomínio Grande", "Endereço", 500);

        Condominium domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(500, domain.getUnidades());
    }

    @Test
    void toDomainWithSpecialCharactersInName() {
        String specialName = "Condomínio São José - Edifício \"A\" & Bloco 'B'";
        CondominiumCreateDTO dto = new CondominiumCreateDTO(specialName, "Rua Especial, 789", 10);

        Condominium domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(specialName, domain.getName());
    }

    @Test
    void toDomainWithLongAddress() {
        String longAddress = "Rua Muito Comprida com Número Bem Grande, Bairro Especial, " +
                            "Cidade Importante, Estado XY, CEP 12345-678, Complemento: Apto 101";
        CondominiumCreateDTO dto = new CondominiumCreateDTO("Condomínio", longAddress, 5);

        Condominium domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(longAddress, domain.getAddress());
    }

    @Test
    void toResponsePreservesAllFields() {
        UUID id = UUID.randomUUID();
        Condominium condominium = Condominium.builder()
                .id(id)
                .name("Teste")
                .address("Endereço")
                .unidades(10)
                .createdAt(LocalDate.now())
                .build();

        CondominiumResponseDTO dto = mapper.toResponse(condominium);

        assertNotNull(dto);
        assertEquals(condominium.getId(), dto.getId());
        assertEquals(condominium.getName(), dto.getName());
        assertEquals(condominium.getAddress(), dto.getAddress());
        assertEquals(condominium.getUnidades(), dto.getUnidades());
        assertEquals(condominium.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    void roundTripConversionPreservesData() {
        String originalName = "Condomínio Test";
        String originalAddress = "Rua Test, 999";
        Integer originalUnidades = 25;

        // DTO -> Domain
        CondominiumCreateDTO dto = new CondominiumCreateDTO(originalName, originalAddress, originalUnidades);
        Condominium domain = mapper.toDomain(dto);

        // Domain -> Response (assuming we add the required fields)
        LocalDate now = LocalDate.now();
        domain.setId(UUID.randomUUID());
        domain.setCreatedAt(now);

        CondominiumResponseDTO response = mapper.toResponse(domain);

        assertEquals(originalName, response.getName());
        assertEquals(originalAddress, response.getAddress());
        assertEquals(originalUnidades, response.getUnidades());
    }

    @Test
    void toDomainCreatesNewInstanceEachTime() {
        CondominiumCreateDTO dto = new CondominiumCreateDTO("Condomínio", "Endereço", 10);

        Condominium domain1 = mapper.toDomain(dto);
        Condominium domain2 = mapper.toDomain(dto);

        assertNotNull(domain1);
        assertNotNull(domain2);
        assertEquals(domain1.getName(), domain2.getName());
    }

    @Test
    void toResponseWithNullCreatedAt() {
        Condominium condominium = Condominium.builder()
                .id(UUID.randomUUID())
                .name("Condomínio")
                .address("Endereço")
                .unidades(10)
                .createdAt(null)
                .build();

        CondominiumResponseDTO dto = mapper.toResponse(condominium);

        assertNotNull(dto);
        assertNull(dto.getCreatedAt());
    }

    @Test
    void toDomainIgnoresIdFieldWhenPresent() {
        CondominiumCreateDTO dto = new CondominiumCreateDTO("Condomínio", "Endereço", 15);
        Condominium domain = mapper.toDomain(dto);

        assertNotNull(domain);
        // ID should not be set by toDomain as it's only for creation
        assertNull(domain.getId());
    }
}



