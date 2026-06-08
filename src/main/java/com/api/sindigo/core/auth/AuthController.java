package com.api.sindigo.core.auth;

import com.api.sindigo.core.auth.dto.LoginResponseDTO;
import com.api.sindigo.core.auth.security.JwtService;
import com.api.sindigo.core.ratelimit.RateLimited;
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

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String VALID = "valid";
    private static final String REASON = "reason";
    private static final String EXPIRES_AT = "expiresAt";

    private static final String TOKEN_NOT_PROVIDED_MESSAGE = "Token não fornecido";
    private static final String TOKEN_VALIDATION_ERROR_MESSAGE = "Erro ao validar token";

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @RateLimited(maxRequests = 5, windowSeconds = 60, message = "Too many registration attempts. Please try again later.")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequestDTO dto) {
        try {
            AuthResponseDTO response = userService.registerUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/login")
    @RateLimited(maxRequests = 5, windowSeconds = 60, message = "Too many login attempts. Please try again later.")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            LoginResponseDTO response = userService.loginUser(dto);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/create-admin")
    @RateLimited(maxRequests = 3, windowSeconds = 300, message = "Too many admin creation attempts. Please try again in 5 minutes.")
    public ResponseEntity<Object> createAdmin(@Valid @RequestBody CreateAdminRequestDTO dto) {
        try {
            AuthResponseDTO response = userService.createAdmin(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(VALID, false, REASON, TOKEN_NOT_PROVIDED_MESSAGE));
            }

            String token = authHeader.substring(7);
            JwtService.JwtValidationResponse validation = jwtService.validateToken(token);

            if (validation.isValid()) {
                return ResponseEntity.ok(Map.of(
                        VALID, true,
                        EXPIRES_AT, validation.getExpiresAt()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    VALID, false,
                    REASON, validation.getReason()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(VALID, false, REASON, TOKEN_VALIDATION_ERROR_MESSAGE));
        }
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