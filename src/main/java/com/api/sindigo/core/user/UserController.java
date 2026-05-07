package com.api.sindigo.core.user;

import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.ChangeRoleRequestDTO;
import com.api.sindigo.core.user.dto.UserResponseDTO;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.core.user.entities.UserRole;
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

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    public User index(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                throw new RuntimeException("Usuário não autenticado");
            }
            
            String userId = (String) principal;
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            UserResponseDTO response = UserResponseDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .createdAt(user.getCreatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", java.time.Instant.now());
            error.put("status", HttpStatus.UNAUTHORIZED.value());
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PutMapping("/change-role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> changeUserRole(@RequestBody ChangeRoleRequestDTO dto, Authentication authentication) {
        try {
            // Validar se is ADMIN
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                throw new RuntimeException("Usuário não autenticado");
            }

            String userId = (String) principal;
            User adminUser = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            if (adminUser.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Apenas ADMIN pode alterar roles"));
            }

            AuthResponseDTO response = userService.changeUserRole(dto.getUserId(), dto.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", java.time.Instant.now());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            // Validar autenticação
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Não autenticado"));
            }

            // Validar se é ADMIN
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                throw new RuntimeException("Usuário não autenticado");
            }

            String userId = (String) principal;
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            if (user.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Apenas ADMIN pode listar usuários"));
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
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", java.time.Instant.now());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

