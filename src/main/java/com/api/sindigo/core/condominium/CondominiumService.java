package com.api.sindigo.core.condominium;

import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.entities.Condominium;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CondominiumService {

    private final CondominiumRepository condominiumRepository;
    private final CondominiumDtoMapper condominiumDtoMapper;

    public CondominiumResponseDTO createCondominium(CondominiumCreateDTO dto) {
        Condominium condominium = condominiumDtoMapper.toDomain(dto);
        Condominium savedCondominium = condominiumRepository.save(condominium);
        return condominiumDtoMapper.toResponse(savedCondominium);
    }

    public List<CondominiumResponseDTO> listCondominiums() {
        return condominiumRepository.findAll()
                .stream()
                .map(condominiumDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CondominiumResponseDTO getById(UUID id) {
        Condominium condominium = condominiumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condominium not found"));
        return condominiumDtoMapper.toResponse(condominium);
    }
}

