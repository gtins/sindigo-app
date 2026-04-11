package com.api.sindigo.core.user;

import com.api.sindigo.core.auth.dto.LoginResponseDTO;
import com.api.sindigo.core.auth.security.JwtTokenProvider;
import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.LoginRequestDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.core.user.validator.UserValidator;
import com.api.sindigo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
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
                .build();

        User savedUser = userRepository.save(newUser);

        return AuthResponseDTO.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
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
                .build();
    }
}
