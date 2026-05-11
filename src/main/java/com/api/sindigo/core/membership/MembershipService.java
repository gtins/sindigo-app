package com.api.sindigo.core.membership;

import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.membership.dto.MembershipResponseDTO;
import com.api.sindigo.core.membership.entities.Membership;
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
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final CondominiumRepository condominiumRepository;
    private final UserRepository userRepository;

    @Transactional
    public MembershipResponseDTO addMember(UUID condominiumId, UUID userId) {
        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Condomínio não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (membershipRepository.existsByCondominiumIdAndUserId(condominiumId, userId)) {
            throw new IllegalArgumentException("Usuário já é membro deste condomínio");
        }

        Membership membership = Membership.builder()
                .condominium(condominium)
                .user(user)
                .build();

        Membership saved = membershipRepository.save(membership);
        log.info("✓ Morador adicionado: {} ao condomínio {}", user.getEmail(), condominium.getName());

        return MembershipResponseDTO.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())
                .userEmail(saved.getUser().getEmail())
                .userName(saved.getUser().getName())
                .condominiumId(saved.getCondominium().getId())
                .condominiumName(saved.getCondominium().getName())
                .joinedAt(saved.getJoinedAt())
                .message("Morador adicionado com sucesso")
                .build();
    }

    @Transactional(readOnly = true)
    public List<MembershipResponseDTO> getMembers(UUID condominiumId) {
        List<Membership> memberships = membershipRepository.findByCondominiumId(condominiumId);
        
        return memberships.stream()
                .map(m -> MembershipResponseDTO.builder()
                        .id(m.getId())
                        .userId(m.getUser().getId())
                        .userEmail(m.getUser().getEmail())
                        .userName(m.getUser().getName())
                        .condominiumId(m.getCondominium().getId())
                        .condominiumName(m.getCondominium().getName())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeMember(UUID condominiumId, UUID userId) {
        membershipRepository.deleteByCondominiumIdAndUserId(condominiumId, userId);
        log.info("✓ Morador removido do condomínio");
    }

    @Transactional(readOnly = true)
    public List<UUID> getUserCondominiums(UUID userId) {
        return membershipRepository.findByUserId(userId)
                .stream()
                .map(m -> m.getCondominium().getId())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isMemberOf(UUID userId, UUID condominiumId) {
        return membershipRepository.existsByCondominiumIdAndUserId(condominiumId, userId);
    }

     @Transactional(readOnly = true)
     public List<MembershipResponseDTO> getCondominiumsByUser(UUID userId) {
         List<Membership> memberships = membershipRepository.findByUserId(userId);
         
         log.info("=== DEBUG getCondominiumsByUser ===");
         log.info("UserID: {}", userId);
         log.info("Memberships encontrados: {}", memberships.size());
         for (Membership m : memberships) {
             log.info("  - Condominium: {} | User Role: {}", m.getCondominium().getName(), m.getUser().getRole());
         }
         
         return memberships.stream()
                 .map(m -> MembershipResponseDTO.builder()
                         .id(m.getId())
                         .userId(m.getUser().getId())
                         .userEmail(m.getUser().getEmail())
                         .userName(m.getUser().getName())
                         .condominiumId(m.getCondominium().getId())
                         .condominiumName(m.getCondominium().getName())
                         .joinedAt(m.getJoinedAt())
                         .build())
                 .collect(Collectors.toList());
     }
}
