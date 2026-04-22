package com.api.sindigo.core.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponseDTO {

    private UUID id;
    private String name;
    private String serviceType;
    private String phone;
    private String email;
    private String notes;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    private UUID condominiumId;
    private List<UUID> activityIds;
}

