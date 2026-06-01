package com.api.sindigo.core.user;

import com.api.sindigo.core.auth.dto.LoginResponseDTO;
import com.api.sindigo.core.auth.security.JwtTokenProvider;
import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.CreateAdminRequestDTO;
import com.api.sindigo.core.user.dto.LoginRequestDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.core.user.entities.UserRole;
import com.api.sindigo.core.user.validator.UserValidator;
import com.api.sindigo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);
    private final UserValidator userValidator = new UserValidator();
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final UserService userService = new UserService(userRepository, passwordEncoder, userValidator, jwtTokenProvider);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "adminSecretKey", "super-secret-key");
    }

    @Test
    void registerUserCreatesStandardResidentAccount() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("Maria Souza")
                .email("maria@example.com")
                .password("senha123")
                .build();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            user.setCreatedAt(LocalDate.of(2026, 6, 1));
            return user;
        });

        AuthResponseDTO response = userService.registerUser(dto);

        assertEquals("Maria Souza", response.getName());
        assertEquals("maria@example.com", response.getEmail());
        assertEquals(UserRole.MORADOR, response.getRole());
        assertEquals("Usuário cadastrado com sucesso", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUserRejectsDuplicatedEmail() {
        RegisterRequestDTO dto = RegisterRequestDTO.builder()
                .name("Maria Souza")
                .email("maria@example.com")
                .password("senha123")
                .build();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(User.builder().build()));

        assertThrows(
                com.api.sindigo.exception.ValidationException.class,
                () -> userService.registerUser(dto)
        );
    }

    @Test
    void loginUserReturnsBearerTokenResponse() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Maria Souza")
                .email("maria@example.com")
                .passwordHash("hashed-password")
                .role(UserRole.ADMIN)
                .build();

        LoginRequestDTO dto = new LoginRequestDTO("maria@example.com", "senha123");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);

        LoginResponseDTO response = userService.loginUser(dto);

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(UserRole.ADMIN, response.getRole());
    }

    @Test
    void loginUserRejectsInvalidPassword() {
        User user = User.builder()
                .email("maria@example.com")
                .passwordHash("hashed-password")
                .build();

        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.loginUser(new LoginRequestDTO("maria@example.com", "wrong-password"))
        );
    }

    @Test
    void createAdminCreatesAdminWhenSecretMatches() {
        CreateAdminRequestDTO dto = new CreateAdminRequestDTO("Admin", "admin@example.com", "senha123", "super-secret-key");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed-admin-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            user.setCreatedAt(LocalDate.of(2026, 6, 1));
            return user;
        });

        AuthResponseDTO response = userService.createAdmin(dto);

        assertEquals("Admin", response.getName());
        assertEquals("admin@example.com", response.getEmail());
        assertEquals(UserRole.ADMIN, response.getRole());
        assertEquals("Admin criado com sucesso", response.getMessage());
    }

    @Test
    void changeUserRoleUpdatesRoleForExistingUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Maria Souza")
                .email("maria@example.com")
                .role(UserRole.MORADOR)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDTO response = userService.changeUserRole(userId, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, response.getRole());
        assertEquals("Papel do usuário alterado com sucesso", response.getMessage());
    }

    @Test
    void changeUserRoleRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.changeUserRole(userId, UserRole.ADMIN));
    }
}


