package com.api.sindigo.core.membership;

import com.api.sindigo.core.membership.dto.MembershipResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MembershipControllerTest {

    private final MembershipService membershipService = mock(MembershipService.class);
    private final MembershipController controller = new MembershipController(membershipService);

    @Test
    void addMemberReturnsCreatedResponseForAdmin() {
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = adminAuth();
        MembershipResponseDTO response = MembershipResponseDTO.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .userEmail("maria@example.com")
                .userName("Maria Souza")
                .condominiumId(condominiumId)
                .condominiumName("Residencial Alfa")
                .joinedAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .message("Morador adicionado com sucesso")
                .build();

        when(membershipService.addMember(condominiumId, userId)).thenReturn(response);

        var result = controller.addMember(authentication, condominiumId, userId);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void addMemberReturnsForbiddenForNonPrivilegedRole() {
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = moradorAuth();

        var result = controller.addMember(authentication, condominiumId, userId);

        assertEquals(403, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Apenas ADMIN e SINDICO podem adicionar moradores", body.get("error"));
    }

    @Test
    void getMembersReturnsListForSindico() {
        UUID condominiumId = UUID.randomUUID();
        Authentication authentication = sindicoAuth();
        MembershipResponseDTO response = MembershipResponseDTO.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .userEmail("maria@example.com")
                .userName("Maria Souza")
                .condominiumId(condominiumId)
                .condominiumName("Residencial Alfa")
                .joinedAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();

        when(membershipService.getMembers(condominiumId)).thenReturn(List.of(response));

        var result = controller.getMembers(authentication, condominiumId);

        assertEquals(200, result.getStatusCode().value());
        List<?> body = assertInstanceOf(List.class, result.getBody());
        assertEquals(1, body.size());
    }

    @Test
    void removeMemberReturnsSuccessMessageForAdmin() {
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = adminAuth();

        var result = controller.removeMember(authentication, condominiumId, userId);

        assertEquals(200, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Morador removido com sucesso", body.get("message"));
    }

    @Test
    void removeMemberReturnsForbiddenForNonAdmin() {
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = moradorAuth();

        var result = controller.removeMember(authentication, condominiumId, userId);

        assertEquals(403, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Apenas ADMIN pode remover moradores", body.get("error"));
    }

    @Test
    void getMyCondominiumsReturnsUserListWhenAuthenticated() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), "token", List.of(new SimpleGrantedAuthority("ROLE_MORADOR")));
        MembershipResponseDTO response = MembershipResponseDTO.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .userEmail("maria@example.com")
                .userName("Maria Souza")
                .condominiumId(UUID.randomUUID())
                .condominiumName("Residencial Alfa")
                .joinedAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();

        when(membershipService.getCondominiumsByUser(userId)).thenReturn(List.of(response));

        var result = controller.getMyCondominiums(authentication);

        assertEquals(200, result.getStatusCode().value());
        List<?> body = assertInstanceOf(List.class, result.getBody());
        assertEquals(1, body.size());
    }

    @Test
    void getMyCondominiumsReturnsUnauthorizedWhenAuthenticationMissing() {
        var result = controller.getMyCondominiums(null);

        assertEquals(401, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Usuário não autenticado", body.get("error"));
    }

    @Test
    void getMyCondominiumsReturnsEmptyListWhenUserHasNoMemberships() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), "token", List.of(new SimpleGrantedAuthority("ROLE_MORADOR")));
        when(membershipService.getCondominiumsByUser(userId)).thenReturn(List.of());

        var result = controller.getMyCondominiums(authentication);

        assertEquals(200, result.getStatusCode().value());
        List<?> body = assertInstanceOf(List.class, result.getBody());
        assertEquals(0, body.size());
    }

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    private Authentication sindicoAuth() {
        return new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_SINDICO"))
        );
    }

    private Authentication moradorAuth() {
        return new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_MORADOR"))
        );
    }
}


