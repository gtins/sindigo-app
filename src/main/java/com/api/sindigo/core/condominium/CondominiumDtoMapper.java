package com.api.sindigo.core.condominium;

import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.entities.Condominium;
import org.springframework.stereotype.Component;

@Component
public class CondominiumDtoMapper {

    public Condominium toDomain(CondominiumCreateDTO dto) {
        return Condominium.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .build();
    }

    public CondominiumResponseDTO toResponse(Condominium condominium) {
        return CondominiumResponseDTO.builder()
                .id(condominium.getId())
                .name(condominium.getName())
                .address(condominium.getAddress())
                .createdAt(condominium.getCreatedAt())
                .build();
    }
}

