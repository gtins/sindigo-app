package com.api.sindigo.core.condominium;

import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.condominium.validator.CondominiumValidator;
import com.api.sindigo.core.membership.MembershipService;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CondominiumService {

    private final CondominiumRepository condominiumRepository;
    private final CondominiumDtoMapper condominiumDtoMapper;
    private final CondominiumValidator condominiumValidator;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;
    private final MembershipService membershipService;

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

        log.info("=== DEBUG getById ===");
        log.info("Condominium ID: {}", id);
        log.info("Authenticated User ID: {}", authenticatedUserId);

        // Buscar condomínio
        Condominium condominium = condominiumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condominium not found"));

        // Verificar se é owner
        boolean isOwner = condominium.getOwner().getId().equals(authenticatedUserId);
        log.info("Is Owner: {}", isOwner);
        log.info("Owner ID: {}", condominium.getOwner().getId());
        
        // Verificar se é membro
        boolean isMember = membershipService.isMemberOf(authenticatedUserId, id);
        log.info("Is Member: {}", isMember);

        // Permitir acesso apenas se for owner ou membro
        if (!isOwner && !isMember) {
            log.warn("X Access Denied: User {} trying to access condominium {}", authenticatedUserId, id);
            throw new RuntimeException("You don't have access to this condominium");
        }

        log.info("✓ Access Granted to condominium {}", id);
        return condominiumDtoMapper.toResponse(condominium);
    }
}

