package com.api.sindigo.core.user.dto;

import com.api.sindigo.core.user.entities.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private LocalDate createdAt;
}

