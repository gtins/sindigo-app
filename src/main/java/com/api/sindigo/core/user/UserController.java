package com.api.sindigo.core.user;

import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.ChangeRoleRequestDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.core.user.dto.UserResponseDTO;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.core.user.entities.UserRole;
import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
import com.api.sindigo.exception.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final String USER_NOT_FOUND = "Usuário não encontrado";
    private static final String INVALID_USER_ID = "ID de usuário inválido";
    private static final String USER_NOT_AUTHENTICATED = "Usuário não autenticado";
    private static final String NOT_AUTHENTICATED = "Não autenticado";
    private static final String ONLY_ADMIN_CAN_CHANGE_ROLES = "Apenas ADMIN pode alterar roles";
    private static final String ONLY_ADMIN_AND_SINDICO_CAN_LIST_USERS = "Apenas ADMIN e SINDICO podem listar usuários";

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody RegisterRequestDTO dto) {
        try {
            AuthResponseDTO response = userService.registerUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (BusinessRuleException e) {
            return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(Authentication authentication) {
        try {
            UUID userId = extractAuthenticatedUserId(authentication);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

            return ResponseEntity.ok(toUserResponseDTO(user));
        } catch (ValidationException | ResourceNotFoundException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, INVALID_USER_ID);
        }
    }

    @PutMapping("/change-role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Object> changeUserRole(
            @RequestBody ChangeRoleRequestDTO dto,
            Authentication authentication
    ) {
        try {
            UUID userId = extractAuthenticatedUserId(authentication);

            User adminUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

            if (adminUser.getRole() != UserRole.ADMIN) {
                throw new BusinessRuleException(ONLY_ADMIN_CAN_CHANGE_ROLES);
            }

            AuthResponseDTO response = userService.changeUserRole(dto.getUserId(), dto.getRole());
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (ResourceNotFoundException | BusinessRuleException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, INVALID_USER_ID);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllUsers(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return buildErrorResponse(HttpStatus.UNAUTHORIZED, NOT_AUTHENTICATED);
            }

            UUID userId = extractAuthenticatedUserId(authentication);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.SINDICO) {
                throw new BusinessRuleException(ONLY_ADMIN_AND_SINDICO_CAN_LIST_USERS);
            }

            List<UserResponseDTO> response = userRepository.findAll()
                    .stream()
                    .map(this::toUserResponseDTO)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (ResourceNotFoundException | BusinessRuleException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, INVALID_USER_ID);
        }
    }

    private UUID extractAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new ValidationException(USER_NOT_AUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String principalAsString) {
            return UUID.fromString(principalAsString);
        }

        return UUID.fromString(authentication.getName());
    }

    private UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                message
        );

        return ResponseEntity.status(status).body(response);
    }

    private record ErrorResponse(
            Instant timestamp,
            int status,
            String error
    ) {
    }
}