package com.api.sindigo.core.user.dto;

import com.api.sindigo.core.user.entities.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeRoleRequestDTO {
    private UUID userId;
    private UserRole role;
}

