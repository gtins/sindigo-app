package com.api.sindigo.core.membership;

import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.membership.dto.MembershipResponseDTO;
import com.api.sindigo.core.membership.entities.Membership;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MembershipServiceTest {

    private final MembershipRepository membershipRepository = mock(MembershipRepository.class);
    private final CondominiumRepository condominiumRepository = mock(CondominiumRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final MembershipService membershipService = new MembershipService(
            membershipRepository,
            condominiumRepository,
            userRepository
    );

    private UUID condominiumId;
    private UUID userId;
    private Condominium condominium;
    private User user;

    @BeforeEach
    void setUp() {
        condominiumId = UUID.randomUUID();
        userId = UUID.randomUUID();
        condominium = Condominium.builder()
                .id(condominiumId)
                .name("Residencial Alfa")
                .build();
        user = User.builder()
                .id(userId)
                .name("Maria Souza")
                .email("maria@example.com")
                .build();
    }

    @Test
    void addMemberPersistsNewMembership() {
        when(condominiumRepository.findById(condominiumId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(membershipRepository.existsByCondominiumIdAndUserId(condominiumId, userId)).thenReturn(false);
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> {
            Membership membership = invocation.getArgument(0);
            membership.setId(UUID.randomUUID());
            membership.setJoinedAt(LocalDateTime.of(2026, 6, 1, 12, 0));
            return membership;
        });

        MembershipResponseDTO response = membershipService.addMember(condominiumId, userId);

        assertEquals(userId, response.getUserId());
        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals("Morador adicionado com sucesso", response.getMessage());
        verify(membershipRepository).save(any(Membership.class));
    }

    @Test
    void addMemberRejectsDuplicateMembership() {
        when(condominiumRepository.findById(condominiumId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(membershipRepository.existsByCondominiumIdAndUserId(condominiumId, userId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> membershipService.addMember(condominiumId, userId));
    }

    @Test
    void addMemberRejectsMissingCondominium() {
        when(condominiumRepository.findById(condominiumId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> membershipService.addMember(condominiumId, userId));
    }

    @Test
    void addMemberRejectsMissingUser() {
        when(condominiumRepository.findById(condominiumId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> membershipService.addMember(condominiumId, userId));
    }

    @Test
    void getMembersReturnsMappedList() {
        Membership membership = Membership.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .user(user)
                .joinedAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();

        when(membershipRepository.findByCondominiumId(condominiumId)).thenReturn(List.of(membership));

        List<MembershipResponseDTO> response = membershipService.getMembers(condominiumId);

        assertEquals(1, response.size());
        assertEquals("Maria Souza", response.getFirst().getUserName());
    }

    @Test
    void removeMemberDelegatesToRepository() {
        membershipService.removeMember(condominiumId, userId);

        verify(membershipRepository).deleteByCondominiumIdAndUserId(condominiumId, userId);
    }

    @Test
    void getCondominiumsByUserReturnsMappedList() {
        Membership membership = Membership.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .user(user)
                .joinedAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();

        when(membershipRepository.findByUserId(userId)).thenReturn(List.of(membership));

        List<MembershipResponseDTO> response = membershipService.getCondominiumsByUser(userId);

        assertEquals(1, response.size());
        assertEquals("Residencial Alfa", response.getFirst().getCondominiumName());
        assertEquals(userId, response.getFirst().getUserId());
    }

    @Test
    void isMemberOfReturnsRepositoryAnswer() {
        when(membershipRepository.existsByCondominiumIdAndUserId(condominiumId, userId)).thenReturn(false);

        assertFalse(membershipService.isMemberOf(userId, condominiumId));
    }
}

