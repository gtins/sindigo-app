package com.api.sindigo.core.condominium;

import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.condominium.validator.CondominiumValidator;
import com.api.sindigo.core.membership.MembershipService;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CondominiumServiceTest {

    private final CondominiumRepository condominiumRepository = mock(CondominiumRepository.class);
    private final CondominiumDtoMapper condominiumDtoMapper = new CondominiumDtoMapper();
    private final CondominiumValidator condominiumValidator = new CondominiumValidator();
    private final SecurityContextHelper securityContextHelper = mock(SecurityContextHelper.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final MembershipService membershipService = mock(MembershipService.class);
    private final CondominiumService condominiumService = new CondominiumService(
            condominiumRepository,
            condominiumDtoMapper,
            condominiumValidator,
            securityContextHelper,
            userRepository,
            membershipService
    );

    private UUID authenticatedUserId;
    private User owner;
    private Condominium condominium;

    @BeforeEach
    void setUp() {
        authenticatedUserId = UUID.randomUUID();
        owner = User.builder()
                .id(authenticatedUserId)
                .name("Síndico")
                .email("sindico@example.com")
                .build();
        condominium = Condominium.builder()
                .id(UUID.randomUUID())
                .name("Residencial Alfa")
                .address("Rua A, 123")
                .unidades(10)
                .owner(owner)
                .createdAt(LocalDate.of(2026, 6, 1))
                .build();
    }

    @Test
    void createCondominiumPersistsAndReturnsResponse() {
        CondominiumCreateDTO dto = CondominiumCreateDTO.builder()
                .name("Residencial Alfa")
                .address("Rua A, 123")
                .unidades(10)
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(owner));
        when(condominiumRepository.save(any(Condominium.class))).thenAnswer(invocation -> {
            Condominium saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(LocalDate.of(2026, 6, 1));
            return saved;
        });

        CondominiumResponseDTO response = condominiumService.createCondominium(dto);

        assertEquals("Residencial Alfa", response.getName());
        assertEquals("Rua A, 123", response.getAddress());
        assertEquals(10, response.getUnidades());
        verify(condominiumRepository).save(any(Condominium.class));
    }

    @Test
    void getByIdReturnsCondominiumWhenUserIsOwner() {
        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findById(condominium.getId())).thenReturn(Optional.of(condominium));
        when(membershipService.isMemberOf(authenticatedUserId, condominium.getId())).thenReturn(false);

        CondominiumResponseDTO response = condominiumService.getById(condominium.getId());

        assertEquals(condominium.getId(), response.getId());
        assertEquals("Residencial Alfa", response.getName());
    }

    @Test
    void getByIdRejectsAccessWhenUserIsNotOwnerNorMember() {
        UUID otherUserId = UUID.randomUUID();
        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(otherUserId);
        when(condominiumRepository.findById(condominium.getId())).thenReturn(Optional.of(condominium));
        when(membershipService.isMemberOf(otherUserId, condominium.getId())).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> condominiumService.getById(condominium.getId()));
    }

    @Test
    void getByIdThrowsWhenCondominiumIsMissing() {
        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findById(condominium.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> condominiumService.getById(condominium.getId()));
    }

    @Test
    void listCondominiumsReturnsOnlyOwnedItems() {
        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByOwnerId(authenticatedUserId)).thenReturn(List.of(condominium));

        List<CondominiumResponseDTO> response = condominiumService.listCondominiums();

        assertEquals(1, response.size());
        assertEquals("Residencial Alfa", response.getFirst().getName());
    }
}

