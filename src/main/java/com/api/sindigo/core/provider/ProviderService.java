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

@Service
@RequiredArgsConstructor
public class ProviderService {

    private static final String CONDOMINIUM_ACCESS_ERROR_MESSAGE =
            "Condominium not found or you don't have access";

    private static final String PROVIDER_NOT_FOUND_MESSAGE = "Provider not found";

    private final ProviderRepository providerRepository;
    private final CondominiumRepository condominiumRepository;
    private final ProviderValidator providerValidator;
    private final SecurityContextHelper securityContextHelper;

    @Transactional
    public ProviderResponseDTO createProvider(UUID condominiumId, ProviderCreateDTO dto) {
        providerValidator.validateProviderCreation(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        Condominium condominium = condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

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
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        return providerRepository.findByCondominiumIdOrderByNameAsc(condominiumId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProviderResponseDTO getProviderById(UUID condominiumId, UUID providerId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Provider provider = providerRepository.findByIdAndCondominiumId(providerId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(PROVIDER_NOT_FOUND_MESSAGE));

        return mapToDTO(provider);
    }

    @Transactional
    public ProviderResponseDTO updateProvider(UUID condominiumId, UUID providerId, ProviderUpdateDTO dto) {
        providerValidator.validateProviderUpdate(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Provider provider = providerRepository.findByIdAndCondominiumId(providerId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(PROVIDER_NOT_FOUND_MESSAGE));

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
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Provider provider = providerRepository.findByIdAndCondominiumId(providerId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(PROVIDER_NOT_FOUND_MESSAGE));

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
        dto.setActivityIds(provider.getActivities()
                .stream()
                .map(Activity::getId)
                .toList());

        return dto;
    }
}