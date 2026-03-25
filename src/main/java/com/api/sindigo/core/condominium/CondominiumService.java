package com.api.sindigo.core.condominium;

import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.condominium.validator.CondominiumValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CondominiumService {

    private final CondominiumRepository condominiumRepository;
    private final CondominiumDtoMapper condominiumDtoMapper;
    private final CondominiumValidator condominiumValidator;

    @Transactional
    public CondominiumResponseDTO createCondominium(CondominiumCreateDTO dto) {
        condominiumValidator.validateCondominiumCreation(dto);

        Condominium condominium = condominiumDtoMapper.toDomain(dto);

        Condominium savedCondominium = condominiumRepository.save(condominium);

        return condominiumDtoMapper.toResponse(savedCondominium);
    }

    @Transactional(readOnly = true)
    public List<CondominiumResponseDTO> listCondominiums() {
        return condominiumRepository.findAll()
                .stream()
                .map(condominiumDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CondominiumResponseDTO getById(UUID id) {
        Condominium condominium = condominiumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condominium not found"));
        return condominiumDtoMapper.toResponse(condominium);
    }
}

