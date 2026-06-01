package com.api.sindigo.core.user;

import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.ChangeRoleRequestDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.core.user.dto.UserResponseDTO;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.core.user.entities.UserRole;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserController controller = new UserController(userService, userRepository);

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
        assertEquals(response, result.getBody());
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
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Email inválido", body.get("error"));
    }

    @Test
    void getCurrentUserReturnsCurrentUserData() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), "token");
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
    }

    @Test
    void getCurrentUserReturnsBadRequestForInvalidUuid() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("not-a-uuid", "token");

        var result = controller.getCurrentUser(authentication);

        assertEquals(400, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("ID de usuário inválido", body.get("error"));
    }

    @Test
    void changeUserRoleReturnsUpdatedResponseForAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminId.toString(), "token", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
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
        assertEquals(response, result.getBody());
    }

    @Test
    void changeUserRoleReturnsForbiddenForNonAdmin() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), "token", List.of(new SimpleGrantedAuthority("ROLE_MORADOR")));
        User nonAdmin = User.builder().id(userId).role(UserRole.MORADOR).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(nonAdmin));

        var result = controller.changeUserRole(ChangeRoleRequestDTO.builder().userId(UUID.randomUUID()).role(UserRole.ADMIN).build(), authentication);

        assertEquals(403, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Apenas ADMIN pode alterar roles", body.get("error"));
    }

    @Test
    void getAllUsersReturnsUserListForAdmin() {
        UUID adminId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminId.toString(), "token", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        User admin = User.builder().id(adminId).role(UserRole.ADMIN).build();
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
    }

    @Test
    void getAllUsersReturnsUnauthorizedWhenNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        var result = controller.getAllUsers(authentication);

        assertEquals(401, result.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, result.getBody());
        assertEquals("Não autenticado", body.get("error"));
    }
}




