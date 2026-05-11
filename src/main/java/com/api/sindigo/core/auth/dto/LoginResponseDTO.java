package com.api.sindigo.core.auth.dto;

import com.api.sindigo.core.user.entities.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private String type;
    private Long expiresIn;
    private UserRole role;
}

