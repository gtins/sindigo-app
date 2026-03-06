package com.api.sindigo.core.user;

import com.api.sindigo.core.user.dto.AuthResponseDTO;
import com.api.sindigo.core.user.dto.RegisterRequestDTO;
import com.api.sindigo.core.user.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public AuthResponseDTO registerUser(RegisterRequestDTO dto) {
        // Validar email único
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        // Criptografar senha
        String encryptedPassword = passwordEncoder.encode(dto.getPassword());

        // Criar e salvar novo usuário
        User newUser = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(encryptedPassword)
                .build();

        User savedUser = userRepository.save(newUser);

        // Retornar resposta sem expor a senha
        return AuthResponseDTO.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .message("Usuário cadastrado com sucesso")
                .build();
    }
}



