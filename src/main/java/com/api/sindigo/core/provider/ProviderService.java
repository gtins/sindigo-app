package com.api.sindigo.core.provider;

import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.provider.dto.ProviderCreateDTO;
import com.api.sindigo.core.provider.dto.ProviderResponseDTO;
import com.api.sindigo.core.provider.dto.ProviderUpdateDTO;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.provider.validator.ProviderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final CondominiumRepository condominiumRepository;
    private final ProviderValidator providerValidator;
    private final SecurityContextHelper securityContextHelper;

    @Transactional
    public ProviderResponseDTO createProvider(UUID condominiumId, ProviderCreateDTO dto) {
        providerValidator.validateProviderCreation(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        Condominium condominium = condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        Provider provider = new Provider();
        provider.setName(dto.getName());
        provider.setServiceType(dto.getServiceType());
        provider.setPhone(dto.getPhone());
        provider.setEmail(dto.getEmail());
        provider.setNotes(dto.getNotes());
        provider.setCondominium(condominium);

        Provider savedProvider = providerRepository.save(provider);
        return mapToDTO(savedProvider);
    }

    @Transactional(readOnly = true)
    public List<ProviderResponseDTO> getProvidersByCondominium(UUID condominiumId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        List<Provider> providers = providerRepository.findByCondominiumIdOrderByNameAsc(condominiumId);
        return providers.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProviderResponseDTO getProviderById(UUID condominiumId, UUID providerId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        Provider provider = providerRepository.findByIdAndCondominiumId(providerId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        return mapToDTO(provider);
    }

    @Transactional
    public ProviderResponseDTO updateProvider(UUID condominiumId, UUID providerId, ProviderUpdateDTO dto) {
        providerValidator.validateProviderUpdate(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        Provider provider = providerRepository.findByIdAndCondominiumId(providerId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        provider.setName(dto.getName());
        provider.setServiceType(dto.getServiceType());
        provider.setPhone(dto.getPhone());
        provider.setEmail(dto.getEmail());
        provider.setNotes(dto.getNotes());

        Provider updatedProvider = providerRepository.save(provider);
        return mapToDTO(updatedProvider);
    }

    @Transactional
    public void deleteProvider(UUID condominiumId, UUID providerId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        Provider provider = providerRepository.findByIdAndCondominiumId(providerId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        providerRepository.delete(provider);
    }

    private ProviderResponseDTO mapToDTO(Provider provider) {
        ProviderResponseDTO dto = new ProviderResponseDTO();
        dto.setId(provider.getId());
        dto.setName(provider.getName());
        dto.setServiceType(provider.getServiceType());
        dto.setPhone(provider.getPhone());
        dto.setEmail(provider.getEmail());
        dto.setNotes(provider.getNotes());
        dto.setCreatedAt(provider.getCreatedAt());
        dto.setUpdatedAt(provider.getUpdatedAt());
        dto.setCondominiumId(provider.getCondominium().getId());
        dto.setActivityIds(provider.getActivities().stream().map(Activity::getId).collect(Collectors.toList()));
        return dto;
    }
}



