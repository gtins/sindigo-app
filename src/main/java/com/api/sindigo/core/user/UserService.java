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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.admin.secret-key:}")
    private String adminSecretKey;

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public AuthResponseDTO registerUser(RegisterRequestDTO dto) {
        userValidator.validateUserRegistration(dto);

        boolean emailExists = userRepository.findByEmail(dto.getEmail()).isPresent();
        userValidator.validateEmailUnique(emailExists);

        String encryptedPassword = passwordEncoder.encode(dto.getPassword());

        User newUser = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(encryptedPassword)
                .role(UserRole.MORADOR) // Todo novo usuário é morador por padrão
                .build();

        User savedUser = userRepository.save(newUser);

        return AuthResponseDTO.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .message("Usuário cadastrado com sucesso")
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO loginUser(LoginRequestDTO dto) {
        // Buscar usuário por email
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Credenciais inválidas"));

        // Validar senha
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new ResourceNotFoundException("Credenciais inválidas");
        }

        // Gerar token JWT
        String token = jwtTokenProvider.generateToken(user);
        long expirationSeconds = jwtTokenProvider.getExpirationTime();

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(expirationSeconds)
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponseDTO createAdmin(CreateAdminRequestDTO dto) {
        // Validar chave secreta
        if (adminSecretKey == null || adminSecretKey.isEmpty()) {
            throw new IllegalArgumentException("Criação de admin não está configurada");
        }
        
        if (!adminSecretKey.equals(dto.getSecretKey())) {
            throw new IllegalArgumentException("Chave secreta inválida");
        }

        // Verificar se email já existe
        boolean emailExists = userRepository.findByEmail(dto.getEmail()).isPresent();
        if (emailExists) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        // Validar dados
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        String encryptedPassword = passwordEncoder.encode(dto.getPassword());

        User newAdmin = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(encryptedPassword)
                .role(UserRole.ADMIN)
                .build();

        User savedAdmin = userRepository.save(newAdmin);

        return AuthResponseDTO.builder()
                .id(savedAdmin.getId())
                .name(savedAdmin.getName())
                .email(savedAdmin.getEmail())
                .role(savedAdmin.getRole())
                .createdAt(savedAdmin.getCreatedAt())
                .message("Admin criado com sucesso")
                .build();
    }

    @Transactional
    public AuthResponseDTO changeUserRole(java.util.UUID userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        return AuthResponseDTO.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole())
                .createdAt(updatedUser.getCreatedAt())
                .message("Papel do usuário alterado com sucesso")
                .build();
    }
}


