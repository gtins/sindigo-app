package com.api.sindigo.core.membership;

import com.api.sindigo.core.membership.dto.MembershipResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        MembershipResponseDTO body = assertInstanceOf(MembershipResponseDTO.class, result.getBody());
        assertEquals(userId, body.getUserId());
        assertEquals(condominiumId, body.getCondominiumId());
        assertEquals("Maria Souza", body.getUserName());
    }

    @Test
    void addMemberReturnsForbiddenForNonPrivilegedRole() {
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = moradorAuth();

        var result = controller.addMember(authentication, condominiumId, userId);

        assertEquals(403, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), "Apenas ADMIN e SINDICO podem adicionar moradores");
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

        MembershipResponseDTO firstMember = assertInstanceOf(MembershipResponseDTO.class, body.get(0));
        assertEquals("Maria Souza", firstMember.getUserName());
        assertEquals(condominiumId, firstMember.getCondominiumId());
    }

    @Test
    void removeMemberReturnsSuccessMessageForAdmin() {
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = adminAuth();

        var result = controller.removeMember(authentication, condominiumId, userId);

        assertEquals(200, result.getStatusCode().value());
        assertSuccessResponse(result.getBody(), "Morador removido com sucesso");
    }

    @Test
    void removeMemberReturnsForbiddenForNonAdmin() {
        UUID condominiumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = moradorAuth();

        var result = controller.removeMember(authentication, condominiumId, userId);

        assertEquals(403, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), "Apenas ADMIN pode remover moradores");
    }

    @Test
    void getMyCondominiumsReturnsUserListWhenAuthenticated() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(userId, "ROLE_MORADOR");

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

        MembershipResponseDTO firstCondominium = assertInstanceOf(MembershipResponseDTO.class, body.get(0));
        assertEquals(userId, firstCondominium.getUserId());
        assertEquals("Residencial Alfa", firstCondominium.getCondominiumName());
    }

    @Test
    void getMyCondominiumsReturnsUnauthorizedWhenAuthenticationMissing() {
        var result = controller.getMyCondominiums(null);

        assertEquals(401, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), "Usuário não autenticado");
    }

    @Test
    void getMyCondominiumsReturnsEmptyListWhenUserHasNoMemberships() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(userId, "ROLE_MORADOR");

        when(membershipService.getCondominiumsByUser(userId)).thenReturn(List.of());

        var result = controller.getMyCondominiums(authentication);

        assertEquals(200, result.getStatusCode().value());
        List<?> body = assertInstanceOf(List.class, result.getBody());
        assertEquals(0, body.size());
        assertNotNull(body);
    }

    private Authentication adminAuth() {
        return authenticatedUser(UUID.randomUUID(), "ROLE_ADMIN");
    }

    private Authentication sindicoAuth() {
        return authenticatedUser(UUID.randomUUID(), "ROLE_SINDICO");
    }

    private Authentication moradorAuth() {
        return authenticatedUser(UUID.randomUUID(), "ROLE_MORADOR");
    }

    private Authentication authenticatedUser(UUID userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId.toString(),
                "token",
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    private void assertErrorResponse(Object body, String expectedError) {
        assertNotNull(body);
        assertEquals(expectedError, readField(body, "error"));
    }

    private void assertSuccessResponse(Object body, String expectedMessage) {
        assertNotNull(body);
        assertEquals(expectedMessage, readField(body, "message"));
    }

    private Object readField(Object body, String accessorName) {
        try {
            Method method = body.getClass().getDeclaredMethod(accessorName);
            method.setAccessible(true);
            return method.invoke(body);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not read response field: " + accessorName, e);
        }
    }
}