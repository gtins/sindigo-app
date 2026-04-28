package com.api.sindigo.core.auth;

import com.api.sindigo.core.auth.dto.LoginResponseDTO;
import com.api.sindigo.core.auth.security.JwtService;
import com.api.sindigo.core.user.UserService;
import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.CreateAdminRequestDTO;
import com.api.sindigo.core.user.dto.LoginRequestDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto) {
        try {
            AuthResponseDTO response = userService.registerUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Email duplicado
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", java.time.Instant.now());
            error.put("status", HttpStatus.CONFLICT.value());
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            LoginResponseDTO response = userService.loginUser(dto);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            // Credenciais inválidas
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", java.time.Instant.now());
            error.put("status", HttpStatus.UNAUTHORIZED.value());
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequestDTO dto) {
        try {
            AuthResponseDTO response = userService.createAdmin(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Erro de validação ou chave secreta inválida
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", java.time.Instant.now());
            error.put("status", HttpStatus.FORBIDDEN.value());
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "reason", "Token não fornecido"));
            }

            String token = authHeader.substring(7);
            JwtService.JwtValidationResponse validation = jwtService.validateToken(token);

            if (validation.isValid()) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "expiresAt", validation.getExpiresAt()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "reason", validation.getReason()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "reason", "Erro ao validar token"));
        }
    }
}


