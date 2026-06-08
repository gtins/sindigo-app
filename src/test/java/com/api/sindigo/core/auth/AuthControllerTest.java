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

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        AuthResponseDTO body = assertInstanceOf(AuthResponseDTO.class, result.getBody());
        assertEquals(response.getId(), body.getId());
        assertEquals("Maria Souza", body.getName());
        assertEquals("maria@example.com", body.getEmail());
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
        assertErrorResponse(result.getBody(), 409, "Email já cadastrado");
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
        LoginResponseDTO body = assertInstanceOf(LoginResponseDTO.class, result.getBody());
        assertEquals("jwt-token", body.getToken());
        assertEquals("Bearer", body.getType());
        assertEquals(UserRole.ADMIN, body.getRole());
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() {
        LoginRequestDTO dto = new LoginRequestDTO("maria@example.com", "senha123");

        when(userService.loginUser(dto)).thenThrow(new ResourceNotFoundException("Credenciais inválidas"));

        var result = controller.login(dto);

        assertEquals(401, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 401, "Credenciais inválidas");
    }

    @Test
    void createAdminReturnsCreatedResponse() {
        CreateAdminRequestDTO dto = new CreateAdminRequestDTO(
                "Admin",
                "admin@example.com",
                "senha123",
                "secret"
        );

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
        AuthResponseDTO body = assertInstanceOf(AuthResponseDTO.class, result.getBody());
        assertEquals("Admin", body.getName());
        assertEquals("admin@example.com", body.getEmail());
        assertEquals(UserRole.ADMIN, body.getRole());
    }

    @Test
    void createAdminReturnsForbiddenWhenSecretIsInvalid() {
        CreateAdminRequestDTO dto = new CreateAdminRequestDTO(
                "Admin",
                "admin@example.com",
                "senha123",
                "secret"
        );

        when(userService.createAdmin(dto)).thenThrow(new IllegalArgumentException("Chave secreta inválida"));

        var result = controller.createAdmin(dto);

        assertEquals(403, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 403, "Chave secreta inválida");
    }

    @Test
    void validateTokenReturnsValidResponseWhenJwtIsOk() {
        Date expiresAt = new Date();

        when(jwtService.validateToken("token-ok"))
                .thenReturn(new JwtService.JwtValidationResponse(true, "Token válido", expiresAt));

        var result = controller.validateToken("Bearer token-ok");

        assertEquals(200, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals(true, body.get("valid"));
        assertEquals(expiresAt, body.get("expiresAt"));
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

    private void assertErrorResponse(Object body, int expectedStatus, String expectedError) {
        assertNotNull(body);
        assertNotNull(readField(body, "timestamp"));
        assertEquals(expectedStatus, readField(body, "status"));
        assertEquals(expectedError, readField(body, "error"));
    }

    private Object readField(Object body, String accessorName) {
        try {
            Method method = body.getClass().getDeclaredMethod(accessorName);
            method.setAccessible(true);
            return method.invoke(body);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not read error response field: " + accessorName, e);
        }
    }
}