package com.api.sindigo.core.membership;

import com.api.sindigo.core.membership.dto.MembershipResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/condominiums")
@RequiredArgsConstructor
@Slf4j
public class MembershipController {

    private static final String ERROR = "error";

    private final MembershipService membershipService;

    /**
     * POST /condominium/{condominiumId}/members/{userId}
     * Adiciona um morador a um condomínio
     * Apenas ADMIN e SINDICO conseguem
     */
    @PostMapping("/{condominiumId}/members/{userId}")
    public ResponseEntity<?> addMember(
            Authentication authentication,
            @PathVariable UUID condominiumId,
            @PathVariable UUID userId) {
        
        try {
            // Validar se é ADMIN ou SINDICO
            if (!isAdminOrSindico(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(ERROR, "Apenas ADMIN e SINDICO podem adicionar moradores"));
            }

            MembershipResponseDTO response = membershipService.addMember(condominiumId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR, "Erro ao adicionar morador"));
        }
    }

    /**
     * GET /condominium/{condominiumId}/members
     * Lista todos os moradores de um condomínio
     */
    @GetMapping("/{condominiumId}/members")
    public ResponseEntity<?> getMembers(
            Authentication authentication,
            @PathVariable UUID condominiumId) {
        
        try {
            // Validar se é ADMIN ou SINDICO
            if (!isAdminOrSindico(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(ERROR, "Apenas ADMIN e SINDICO podem listar moradores"));
            }

            List<MembershipResponseDTO> members = membershipService.getMembers(condominiumId);
            return ResponseEntity.ok(members);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR, "Erro ao listar moradores"));
        }
    }

    /**
     * DELETE /condominium/{condominiumId}/members/{userId}
     * Remove um morador de um condomínio
     * Apenas ADMIN consegue
     */
    @DeleteMapping("/{condominiumId}/members/{userId}")
    public ResponseEntity<?> removeMember(
            Authentication authentication,
            @PathVariable UUID condominiumId,
            @PathVariable UUID userId) {
        
        try {
            // Validar se é ADMIN
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(ERROR, "Apenas ADMIN pode remover moradores"));
            }

            membershipService.removeMember(condominiumId, userId);
            return ResponseEntity.ok(Map.of("message", "Morador removido com sucesso"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR, "Erro ao remover morador"));
        }
    }

     /**
      * GET /condominiums/my-condominiums
      * Retorna os condomínios onde o usuário autenticado foi adicionado como membro
      * Funciona para qualquer role: ADMIN, SINDICO, MORADOR
      * Diferente de GET /condominiums que retorna apenas os criados pelo usuário
      */
     @GetMapping("/my-condominiums")
     public ResponseEntity<?> getMyCondominiums(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(ERROR, "Usuário não autenticado"));
            }

            Object principal = authentication.getPrincipal();
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(ERROR, "Usuário não autenticado"));
            }

            String userId = (String) principal;
            List<MembershipResponseDTO> condominiums = membershipService.getCondominiumsByUser(UUID.fromString(userId));
            return ResponseEntity.ok(condominiums);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR, "Erro ao buscar condomínios"));
        }
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isAdminOrSindico(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                              a.getAuthority().equals("ROLE_SINDICO"));
    }
}

