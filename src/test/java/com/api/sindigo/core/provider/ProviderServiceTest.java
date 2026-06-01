package com.api.sindigo.core.provider;

import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.provider.dto.ProviderCreateDTO;
import com.api.sindigo.core.provider.dto.ProviderResponseDTO;
import com.api.sindigo.core.provider.dto.ProviderUpdateDTO;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.provider.validator.ProviderValidator;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProviderServiceTest {

    private final ProviderRepository providerRepository = mock(ProviderRepository.class);
    private final CondominiumRepository condominiumRepository = mock(CondominiumRepository.class);
    private final ProviderValidator providerValidator = new ProviderValidator();
    private final SecurityContextHelper securityContextHelper = mock(SecurityContextHelper.class);
    private final ProviderService providerService = new ProviderService(
            providerRepository,
            condominiumRepository,
            providerValidator,
            securityContextHelper
    );

    private UUID authenticatedUserId;
    private UUID condominiumId;
    private Condominium condominium;

    @BeforeEach
    void setUp() {
        authenticatedUserId = UUID.randomUUID();
        condominiumId = UUID.randomUUID();
        condominium = Condominium.builder()
                .id(condominiumId)
                .owner(User.builder().id(authenticatedUserId).build())
                .name("Residencial Alfa")
                .build();
    }

    @Test
    void createProviderPersistsAndMapsResponse() {
        ProviderCreateDTO dto = new ProviderCreateDTO(
                "Carlos Manutenção",
                "Elétrica",
                "11999999999",
                "carlos@example.com",
                "Atendimento noturno"
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> {
            Provider provider = invocation.getArgument(0);
            provider.setId(UUID.randomUUID());
            provider.setCreatedAt(LocalDate.of(2026, 6, 1));
            return provider;
        });

        ProviderResponseDTO response = providerService.createProvider(condominiumId, dto);

        assertEquals("Carlos Manutenção", response.getName());
        assertEquals("Elétrica", response.getServiceType());
        assertEquals(condominiumId, response.getCondominiumId());
        verify(providerRepository).save(any(Provider.class));
    }

    @Test
    void getProvidersByCondominiumReturnsMappedList() {
        Provider provider = Provider.builder()
                .id(UUID.randomUUID())
                .name("Carlos Manutenção")
                .serviceType("Elétrica")
                .phone("11999999999")
                .email("carlos@example.com")
                .notes("Atendimento noturno")
                .condominium(condominium)
                .activities(Collections.emptyList())
                .createdAt(LocalDate.of(2026, 6, 1))
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(providerRepository.findByCondominiumIdOrderByNameAsc(condominiumId)).thenReturn(List.of(provider));

        List<ProviderResponseDTO> response = providerService.getProvidersByCondominium(condominiumId);

        assertEquals(1, response.size());
        assertEquals("Carlos Manutenção", response.getFirst().getName());
    }

    @Test
    void updateProviderAppliesNewData() {
        UUID providerId = UUID.randomUUID();
        Provider provider = Provider.builder()
                .id(providerId)
                .name("Carlos Manutenção")
                .serviceType("Elétrica")
                .phone("11999999999")
                .email("carlos@example.com")
                .condominium(condominium)
                .activities(Collections.emptyList())
                .build();
        ProviderUpdateDTO dto = new ProviderUpdateDTO("Carlos Atualizado", "Hidráulica", "11888888888", "carlos.novo@example.com", null);

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(providerRepository.findByIdAndCondominiumId(providerId, condominiumId)).thenReturn(Optional.of(provider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderResponseDTO response = providerService.updateProvider(condominiumId, providerId, dto);

        assertEquals("Carlos Atualizado", response.getName());
        assertEquals("Hidráulica", response.getServiceType());
        assertEquals("carlos.novo@example.com", response.getEmail());
    }

    @Test
    void deleteProviderRemovesExistingProvider() {
        UUID providerId = UUID.randomUUID();
        Provider provider = Provider.builder().id(providerId).condominium(condominium).activities(Collections.emptyList()).build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(providerRepository.findByIdAndCondominiumId(providerId, condominiumId)).thenReturn(Optional.of(provider));

        providerService.deleteProvider(condominiumId, providerId);

        verify(providerRepository).delete(provider);
    }

    @Test
    void createProviderRejectsAccessWhenCondominiumDoesNotBelongToOwner() {
        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> providerService.createProvider(
                condominiumId,
                new ProviderCreateDTO("Carlos", "Elétrica", "11999999999", null, null)
        ));
    }

    @Test
    void getProviderByIdReturnsMappedProvider() {
        UUID providerId = UUID.randomUUID();
        Provider provider = Provider.builder()
                .id(providerId)
                .name("Carlos Manutenção")
                .serviceType("Elétrica")
                .phone("11999999999")
                .email("carlos@example.com")
                .notes("Atendimento noturno")
                .condominium(condominium)
                .activities(Collections.emptyList())
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(providerRepository.findByIdAndCondominiumId(providerId, condominiumId)).thenReturn(Optional.of(provider));

        ProviderResponseDTO response = providerService.getProviderById(condominiumId, providerId);

        assertEquals(providerId, response.getId());
        assertEquals("Carlos Manutenção", response.getName());
    }

    @Test
    void getProviderByIdRejectsMissingProvider() {
        UUID providerId = UUID.randomUUID();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(providerRepository.findByIdAndCondominiumId(providerId, condominiumId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> providerService.getProviderById(condominiumId, providerId));
    }

    @Test
    void updateProviderRejectsMissingCondominium() {
        UUID providerId = UUID.randomUUID();
        ProviderUpdateDTO dto = new ProviderUpdateDTO("Carlos", "Elétrica", "11999999999", "carlos@example.com", null);

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> providerService.updateProvider(condominiumId, providerId, dto));
    }

    @Test
    void deleteProviderRejectsMissingProvider() {
        UUID providerId = UUID.randomUUID();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(providerRepository.findByIdAndCondominiumId(providerId, condominiumId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> providerService.deleteProvider(condominiumId, providerId));
    }
}

