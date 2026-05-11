package com.api.sindigo.core.user;

import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.ChangeRoleRequestDTO;
import com.api.sindigo.core.user.dto.UserResponseDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final String USER_NOT_FOUND = "Usuário não encontrado";
    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequestDTO dto) {
        try {
            AuthResponseDTO response = userService.registerUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.BAD_REQUEST.value());
            error.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (BusinessRuleException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.CONFLICT.value());
            error.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                throw new ValidationException("Usuário não autenticado");
            }
            
            String userId = (String) principal;
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

            UserResponseDTO response = UserResponseDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .createdAt(user.getCreatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (ValidationException | ResourceNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.UNAUTHORIZED.value());
            error.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.BAD_REQUEST.value());
            error.put(ERROR, "ID de usuário inválido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/change-role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> changeUserRole(@RequestBody ChangeRoleRequestDTO dto, Authentication authentication) {
        try {
            // Validar se is ADMIN
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                throw new ValidationException("Usuário não autenticado");
            }

            String userId = (String) principal;
            User adminUser = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

            if (adminUser.getRole() != UserRole.ADMIN) {
                throw new BusinessRuleException("Apenas ADMIN pode alterar roles");
            }

            AuthResponseDTO response = userService.changeUserRole(dto.getUserId(), dto.getRole());
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.UNAUTHORIZED.value());
            error.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (ResourceNotFoundException | BusinessRuleException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.FORBIDDEN.value());
            error.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.BAD_REQUEST.value());
            error.put(ERROR, "ID de usuário inválido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            // Validar autenticação
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(ERROR, "Não autenticado"));
            }

            // Validar se é ADMIN
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                throw new ValidationException("Usuário não autenticado");
            }

            String userId = (String) principal;
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

            if (user.getRole() != UserRole.ADMIN) {
                throw new BusinessRuleException("Apenas ADMIN pode listar usuários");
            }

            // Buscar todos os usuários
            List<User> allUsers = userRepository.findAll();
            List<UserResponseDTO> response = allUsers.stream()
                    .map(u -> UserResponseDTO.builder()
                            .id(u.getId())
                            .name(u.getName())
                            .email(u.getEmail())
                            .role(u.getRole())
                            .createdAt(u.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.UNAUTHORIZED.value());
            error.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (ResourceNotFoundException | BusinessRuleException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.FORBIDDEN.value());
            error.put(ERROR, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(TIMESTAMP, java.time.Instant.now());
            error.put(STATUS, HttpStatus.BAD_REQUEST.value());
            error.put(ERROR, "ID de usuário inválido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

