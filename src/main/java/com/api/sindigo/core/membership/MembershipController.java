package com.api.sindigo.core.membership;

import com.api.sindigo.core.membership.dto.MembershipResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/condominiums")
@RequiredArgsConstructor
@Slf4j
public class MembershipController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SINDICO = "ROLE_SINDICO";

    private static final String ADD_MEMBER_FORBIDDEN_MESSAGE = "Apenas ADMIN e SINDICO podem adicionar moradores";
    private static final String LIST_MEMBERS_FORBIDDEN_MESSAGE = "Apenas ADMIN e SINDICO podem listar moradores";
    private static final String REMOVE_MEMBER_FORBIDDEN_MESSAGE = "Apenas ADMIN pode remover moradores";
    private static final String USER_NOT_AUTHENTICATED_MESSAGE = "Usuário não autenticado";

    private static final String ADD_MEMBER_ERROR_MESSAGE = "Erro ao adicionar morador";
    private static final String LIST_MEMBERS_ERROR_MESSAGE = "Erro ao listar moradores";
    private static final String REMOVE_MEMBER_ERROR_MESSAGE = "Erro ao remover morador";
    private static final String FETCH_CONDOMINIUMS_ERROR_MESSAGE = "Erro ao buscar condomínios";
    private static final String MEMBER_REMOVED_SUCCESSFULLY_MESSAGE = "Morador removido com sucesso";

    private final MembershipService membershipService;

    @PostMapping("/{condominiumId}/members/{userId}")
    public ResponseEntity<Object> addMember(
            Authentication authentication,
            @PathVariable UUID condominiumId,
            @PathVariable UUID userId) {

        try {
            if (!isAdminOrSindico(authentication)) {
                return buildErrorResponse(HttpStatus.FORBIDDEN, ADD_MEMBER_FORBIDDEN_MESSAGE);
            }

            MembershipResponseDTO response = membershipService.addMember(condominiumId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error(ADD_MEMBER_ERROR_MESSAGE, e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ADD_MEMBER_ERROR_MESSAGE);
        }
    }

    @GetMapping("/{condominiumId}/members")
    public ResponseEntity<Object> getMembers(
            Authentication authentication,
            @PathVariable UUID condominiumId) {

        try {
            if (!isAdminOrSindico(authentication)) {
                return buildErrorResponse(HttpStatus.FORBIDDEN, LIST_MEMBERS_FORBIDDEN_MESSAGE);
            }

            List<MembershipResponseDTO> members = membershipService.getMembers(condominiumId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            log.error(LIST_MEMBERS_ERROR_MESSAGE, e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, LIST_MEMBERS_ERROR_MESSAGE);
        }
    }

    @DeleteMapping("/{condominiumId}/members/{userId}")
    public ResponseEntity<Object> removeMember(
            Authentication authentication,
            @PathVariable UUID condominiumId,
            @PathVariable UUID userId) {

        try {
            if (!isAdmin(authentication)) {
                return buildErrorResponse(HttpStatus.FORBIDDEN, REMOVE_MEMBER_FORBIDDEN_MESSAGE);
            }

            membershipService.removeMember(condominiumId, userId);
            return ResponseEntity.ok(new SuccessResponse(MEMBER_REMOVED_SUCCESSFULLY_MESSAGE));
        } catch (Exception e) {
            log.error(REMOVE_MEMBER_ERROR_MESSAGE, e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, REMOVE_MEMBER_ERROR_MESSAGE);
        }
    }

    @GetMapping("/my-condominiums")
    public ResponseEntity<Object> getMyCondominiums(Authentication authentication) {
        try {
            if (!isAuthenticated(authentication) || authentication.getPrincipal() == null) {
                return buildErrorResponse(HttpStatus.UNAUTHORIZED, USER_NOT_AUTHENTICATED_MESSAGE);
            }

            UUID userId = extractUserId(authentication);
            List<MembershipResponseDTO> condominiums = membershipService.getCondominiumsByUser(userId);

            return ResponseEntity.ok(condominiums);
        } catch (Exception e) {
            log.error(FETCH_CONDOMINIUMS_ERROR_MESSAGE, e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, FETCH_CONDOMINIUMS_ERROR_MESSAGE);
        }
    }

    private UUID extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof String principalAsString) {
            return UUID.fromString(principalAsString);
        }

        return UUID.fromString(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return hasAuthority(authentication, ROLE_ADMIN);
    }

    private boolean isAdminOrSindico(Authentication authentication) {
        return hasAuthority(authentication, ROLE_ADMIN) || hasAuthority(authentication, ROLE_SINDICO);
    }

    private boolean hasAuthority(Authentication authentication, String expectedAuthority) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> expectedAuthority.equals(authority.getAuthority()));
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }

    private record ErrorResponse(String error) {
    }

    private record SuccessResponse(String message) {
    }
}