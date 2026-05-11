package com.api.sindigo.core.membership.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipResponseDTO {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private UUID condominiumId;
    private String condominiumName;
    
    @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, 
                pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime joinedAt;
    
    private String message;
}

