package com.api.sindigo.core.condominium;

import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.dto.CondominiumUpdateDTO;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.condominium.validator.CondominiumValidator;
import com.api.sindigo.core.membership.MembershipService;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Condominium not found"));

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
            throw new BusinessRuleException("You don't have access to this condominium");
        }

        log.info("✓ Access Granted to condominium {}", id);
        return condominiumDtoMapper.toResponse(condominium);
    }

    @Transactional
    public CondominiumResponseDTO updateCondominium(UUID id, CondominiumUpdateDTO dto) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        // Buscar condomínio
        Condominium condominium = condominiumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condominium not found"));

        // Verificar se o usuário autenticado é o dono
        if (!condominium.getOwner().getId().equals(authenticatedUserId)) {
            log.warn("X Access Denied: User {} trying to update condominium {}", authenticatedUserId, id);
            throw new BusinessRuleException("Only the owner can edit this condominium");
        }

        // Atualizar os dados
        condominium.setName(dto.getName());
        condominium.setAddress(dto.getAddress());
        condominium.setUnidades(dto.getUnidades());
        condominium.setActive(dto.getActive());

        Condominium updatedCondominium = condominiumRepository.save(condominium);

        log.info("✓ Condominium {} updated successfully", id);
        return condominiumDtoMapper.toResponse(updatedCondominium);
    }
}

