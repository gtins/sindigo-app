package com.api.sindigo.core.user;

import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.ChangeRoleRequestDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.core.user.dto.UserResponseDTO;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.core.user.entities.UserRole;
import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController controller;

    @Test
    void createUserReturnsCreatedResponse() {
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

        var result = controller.createUser(dto);

        assertEquals(201, result.getStatusCode().value());
        AuthResponseDTO body = assertInstanceOf(AuthResponseDTO.class, result.getBody());
        assertEquals(response.getId(), body.getId());
        assertEquals("Maria Souza", body.getName());
        assertEquals("maria@example.com", body.getEmail());
    }

    @Test
    void createUserReturnsBadRequestForValidationException() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("Maria Souza")
                .email("maria@example.com")
                .password("senha123")
                .build();

        when(userService.registerUser(dto)).thenThrow(new ValidationException("Email inválido"));

        var result = controller.createUser(dto);

        assertEquals(400, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 400, "Email inválido");
    }

    @Test
    void createUserReturnsConflictForBusinessRuleException() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("Maria Souza")
                .email("maria@example.com")
                .password("senha123")
                .build();

        when(userService.registerUser(dto))
                .thenThrow(new BusinessRuleException("Email já cadastrado no sistema"));

        var result = controller.createUser(dto);

        assertEquals(409, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 409, "Email já cadastrado no sistema");
    }

    @Test
    void getCurrentUserReturnsCurrentUserData() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(userId, "ROLE_ADMIN");

        User user = User.builder()
                .id(userId)
                .name("Maria Souza")
                .email("maria@example.com")
                .role(UserRole.ADMIN)
                .createdAt(LocalDate.of(2026, 6, 1))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = controller.getCurrentUser(authentication);

        assertEquals(200, result.getStatusCode().value());
        UserResponseDTO body = assertInstanceOf(UserResponseDTO.class, result.getBody());
        assertEquals(userId, body.getId());
        assertEquals("Maria Souza", body.getName());
        assertEquals("maria@example.com", body.getEmail());
    }

    @Test
    void getCurrentUserReturnsUnauthorizedWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(userId, "ROLE_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        var result = controller.getCurrentUser(authentication);

        assertEquals(401, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 401, "Usuário não encontrado");
    }

    @Test
    void getCurrentUserReturnsBadRequestForInvalidUuid() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "not-a-uuid",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var result = controller.getCurrentUser(authentication);

        assertEquals(400, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 400, "ID de usuário inválido");
    }

    @Test
    void changeUserRoleReturnsUpdatedResponseForAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        Authentication authentication = authenticatedUser(adminId, "ROLE_ADMIN");

        User admin = User.builder()
                .id(adminId)
                .role(UserRole.ADMIN)
                .build();

        ChangeRoleRequestDTO dto = ChangeRoleRequestDTO.builder()
                .userId(targetUserId)
                .role(UserRole.SINDICO)
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .id(targetUserId)
                .name("João")
                .email("joao@example.com")
                .role(UserRole.SINDICO)
                .createdAt(LocalDate.of(2026, 6, 1))
                .message("Papel do usuário alterado com sucesso")
                .build();

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userService.changeUserRole(targetUserId, UserRole.SINDICO)).thenReturn(response);

        var result = controller.changeUserRole(dto, authentication);

        assertEquals(200, result.getStatusCode().value());
        AuthResponseDTO body = assertInstanceOf(AuthResponseDTO.class, result.getBody());
        assertEquals(targetUserId, body.getId());
        assertEquals(UserRole.SINDICO, body.getRole());
        assertEquals("João", body.getName());
    }

    @Test
    void changeUserRoleReturnsForbiddenForNonAdmin() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(userId, "ROLE_MORADOR");

        User nonAdmin = User.builder()
                .id(userId)
                .role(UserRole.MORADOR)
                .build();

        ChangeRoleRequestDTO dto = ChangeRoleRequestDTO.builder()
                .userId(UUID.randomUUID())
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(nonAdmin));

        var result = controller.changeUserRole(dto, authentication);

        assertEquals(403, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 403, "Apenas ADMIN pode alterar roles");
    }

    @Test
    void changeUserRoleReturnsBadRequestForInvalidAuthenticatedPrincipalUuid() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "not-a-uuid",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        ChangeRoleRequestDTO dto = ChangeRoleRequestDTO.builder()
                .userId(UUID.randomUUID())
                .role(UserRole.ADMIN)
                .build();

        var result = controller.changeUserRole(dto, authentication);

        assertEquals(400, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 400, "ID de usuário inválido");
    }

    @Test
    void changeUserRoleReturnsForbiddenWhenAdminNotFound() {
        UUID adminId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(adminId, "ROLE_ADMIN");

        ChangeRoleRequestDTO dto = ChangeRoleRequestDTO.builder()
                .userId(UUID.randomUUID())
                .role(UserRole.SINDICO)
                .build();

        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        var result = controller.changeUserRole(dto, authentication);

        assertEquals(403, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 403, "Usuário não encontrado");
    }

    @Test
    void changeUserRoleReturnsForbiddenWhenUserServiceThrowsBusinessRuleException() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(adminId, "ROLE_ADMIN");

        User admin = User.builder()
                .id(adminId)
                .role(UserRole.ADMIN)
                .build();

        ChangeRoleRequestDTO dto = ChangeRoleRequestDTO.builder()
                .userId(targetUserId)
                .role(UserRole.SINDICO)
                .build();

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userService.changeUserRole(targetUserId, UserRole.SINDICO))
                .thenThrow(new BusinessRuleException("Usuário alvo não encontrado"));

        var result = controller.changeUserRole(dto, authentication);

        assertEquals(403, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 403, "Usuário alvo não encontrado");
    }

    @Test
    void changeUserRoleReturnsUnauthorizedWhenUserServiceThrowsValidationException() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(adminId, "ROLE_ADMIN");

        User admin = User.builder()
                .id(adminId)
                .role(UserRole.ADMIN)
                .build();

        ChangeRoleRequestDTO dto = ChangeRoleRequestDTO.builder()
                .userId(targetUserId)
                .role(UserRole.SINDICO)
                .build();

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userService.changeUserRole(targetUserId, UserRole.SINDICO))
                .thenThrow(new ValidationException("Dados inválidos"));

        var result = controller.changeUserRole(dto, authentication);

        assertEquals(401, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 401, "Dados inválidos");
    }

    @Test
    void getAllUsersReturnsForbiddenWhenAuthenticatedUserNotFound() {
        UUID adminId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(adminId, "ROLE_ADMIN");

        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        var result = controller.getAllUsers(authentication);

        assertEquals(403, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 403, "Usuário não encontrado");
    }

    @Test
    void getCurrentUserReturnsUnauthorizedWhenNotAuthenticated() {
        Authentication authentication = null;

        var result = controller.getCurrentUser(authentication);

        assertEquals(401, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 401, "Usuário não autenticado");
    }

    @Test
    void changeUserRoleReturnsUnauthorizedWhenNotAuthenticated() {
        Authentication authentication = null;

        ChangeRoleRequestDTO dto = ChangeRoleRequestDTO.builder()
                .userId(UUID.randomUUID())
                .role(UserRole.ADMIN)
                .build();

        var result = controller.changeUserRole(dto, authentication);

        assertEquals(401, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 401, "Usuário não autenticado");
    }

    @Test
    void getAllUsersReturnsUserListForAdmin() {
        UUID adminId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(adminId, "ROLE_ADMIN");

        User admin = User.builder()
                .id(adminId)
                .role(UserRole.ADMIN)
                .build();

        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .name("Maria Souza")
                .email("maria@example.com")
                .role(UserRole.MORADOR)
                .createdAt(LocalDate.of(2026, 6, 1))
                .build();

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findAll()).thenReturn(List.of(otherUser));

        var result = controller.getAllUsers(authentication);

        assertEquals(200, result.getStatusCode().value());
        List<?> body = assertInstanceOf(List.class, result.getBody());
        assertEquals(1, body.size());

        UserResponseDTO firstUser = assertInstanceOf(UserResponseDTO.class, body.get(0));
        assertEquals("Maria Souza", firstUser.getName());
        assertEquals("maria@example.com", firstUser.getEmail());
    }

    @Test
    void getAllUsersReturnsUnauthorizedWhenNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        var result = controller.getAllUsers(authentication);

        assertEquals(401, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 401, "Não autenticado");
    }

    @Test
    void getAllUsersReturnsUserListForSindico() {
        UUID sindicoId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(sindicoId, "ROLE_SINDICO");

        User sindico = User.builder()
                .id(sindicoId)
                .role(UserRole.SINDICO)
                .build();

        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .name("Maria Souza")
                .email("maria@example.com")
                .role(UserRole.MORADOR)
                .createdAt(LocalDate.of(2026, 6, 1))
                .build();

        when(userRepository.findById(sindicoId)).thenReturn(Optional.of(sindico));
        when(userRepository.findAll()).thenReturn(List.of(otherUser));

        var result = controller.getAllUsers(authentication);

        assertEquals(200, result.getStatusCode().value());
        List<?> body = assertInstanceOf(List.class, result.getBody());
        assertEquals(1, body.size());
    }

    @Test
    void getAllUsersReturnsForbiddenForMorador() {
        UUID moradorId = UUID.randomUUID();
        Authentication authentication = authenticatedUser(moradorId, "ROLE_MORADOR");

        User morador = User.builder()
                .id(moradorId)
                .role(UserRole.MORADOR)
                .build();

        when(userRepository.findById(moradorId)).thenReturn(Optional.of(morador));

        var result = controller.getAllUsers(authentication);

        assertEquals(403, result.getStatusCode().value());
        assertErrorResponse(result.getBody(), 403, "Apenas ADMIN e SINDICO podem listar usuários");
    }

    private Authentication authenticatedUser(UUID userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId.toString(),
                "token",
                List.of(new SimpleGrantedAuthority(role))
        );
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