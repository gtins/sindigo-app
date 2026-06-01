package com.api.sindigo.core.auth;

import com.api.sindigo.core.auth.dto.LoginResponseDTO;
import com.api.sindigo.core.auth.security.JwtService;
import com.api.sindigo.core.user.UserService;
import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.CreateAdminRequestDTO;
import com.api.sindigo.core.user.dto.LoginRequestDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.core.user.entities.UserRole;
import com.api.sindigo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final UserService userService = mock(UserService.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthController controller = new AuthController(userService, jwtService);

    @Test
    void registerReturnsCreatedResponse() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("Maria Souza")
                .email("maria@example.com")
                .password("senha123")
                .build();
        AuthResponseDTO response = AuthResponseDTO.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .email(dto.getEmail())
                .role(UserRole.MORADOR)
                .createdAt(LocalDate.of(2026, 6, 1))
                .message("Usuário cadastrado com sucesso")
                .build();

        when(userService.registerUser(dto)).thenReturn(response);

        var result = controller.register(dto);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(userService).registerUser(dto);
    }

    @Test
    void registerReturnsConflictWhenServiceThrowsIllegalArgumentException() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("Maria Souza")
                .email("maria@example.com")
                .password("senha123")
                .build();
        when(userService.registerUser(dto)).thenThrow(new IllegalArgumentException("Email já cadastrado"));

        var result = controller.register(dto);

        assertEquals(409, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Email já cadastrado", body.get("error"));
    }

    @Test
    void loginReturnsBearerToken() {
        LoginRequestDTO dto = new LoginRequestDTO("maria@example.com", "senha123");
        LoginResponseDTO response = LoginResponseDTO.builder()
                .token("jwt-token")
                .type("Bearer")
                .expiresIn(3600L)
                .role(UserRole.ADMIN)
                .build();

        when(userService.loginUser(dto)).thenReturn(response);

        var result = controller.login(dto);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() {
        LoginRequestDTO dto = new LoginRequestDTO("maria@example.com", "senha123");
        when(userService.loginUser(dto)).thenThrow(new ResourceNotFoundException("Credenciais inválidas"));

        var result = controller.login(dto);

        assertEquals(401, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Credenciais inválidas", body.get("error"));
    }

    @Test
    void createAdminReturnsCreatedResponse() {
        CreateAdminRequestDTO dto = new CreateAdminRequestDTO("Admin", "admin@example.com", "senha123", "secret");
        AuthResponseDTO response = AuthResponseDTO.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .email(dto.getEmail())
                .role(UserRole.ADMIN)
                .createdAt(LocalDate.of(2026, 6, 1))
                .message("Admin criado com sucesso")
                .build();

        when(userService.createAdmin(dto)).thenReturn(response);

        var result = controller.createAdmin(dto);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void createAdminReturnsForbiddenWhenSecretIsInvalid() {
        CreateAdminRequestDTO dto = new CreateAdminRequestDTO("Admin", "admin@example.com", "senha123", "secret");
        when(userService.createAdmin(dto)).thenThrow(new IllegalArgumentException("Chave secreta inválida"));

        var result = controller.createAdmin(dto);

        assertEquals(403, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Chave secreta inválida", body.get("error"));
    }

    @Test
    void validateTokenReturnsValidResponseWhenJwtIsOk() {
        when(jwtService.validateToken("token-ok")).thenReturn(new JwtService.JwtValidationResponse(true, "Token válido", new java.util.Date()));

        var result = controller.validateToken("Bearer token-ok");

        assertEquals(200, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals(true, body.get("valid"));
        assertTrue(body.containsKey("expiresAt"));
    }

    @Test
    void validateTokenReturnsUnauthorizedWhenHeaderIsMissing() {
        var result = controller.validateToken(null);

        assertEquals(401, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals(false, body.get("valid"));
        assertEquals("Token não fornecido", body.get("reason"));
    }
}


