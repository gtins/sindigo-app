package com.api.sindigo.core.condominium;

import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.condominium.validator.CondominiumValidator;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
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
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @Transactional
    public CondominiumResponseDTO createCondominium(CondominiumCreateDTO dto) {
        condominiumValidator.validateCondominiumCreation(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();
        User owner = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));

        Condominium condominium = condominiumDtoMapper.toDomain(dto);
        condominium.setOwner(owner);

        Condominium savedCondominium = condominiumRepository.save(condominium);

        return condominiumDtoMapper.toResponse(savedCondominium);
    }

    @Transactional(readOnly = true)
    public List<CondominiumResponseDTO> listCondominiums() {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        return condominiumRepository.findByOwnerId(authenticatedUserId)
                .stream()
                .map(condominiumDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CondominiumResponseDTO getById(UUID id) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        Condominium condominium = condominiumRepository.findByIdAndOwnerId(id, authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("Condominium not found or you don't have access"));

        return condominiumDtoMapper.toResponse(condominium);
    }
}

