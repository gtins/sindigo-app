package com.api.sindigo.core.condominium.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CondominiumResponseDTO {

    private UUID id;
    private String name;
    private String address;
    private Integer unidades;
    private Boolean active;
    private LocalDate createdAt;
}

