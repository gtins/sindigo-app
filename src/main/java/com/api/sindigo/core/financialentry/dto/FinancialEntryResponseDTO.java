package com.api.sindigo.core.financialentry.dto;

import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialEntryResponseDTO {

    private UUID id;

    @JsonProperty("condominium_id")
    private UUID condominiumId;

    private FinancialEntryType type;
    private BigDecimal amount;
    private LocalDate date;
    private String description;

    @JsonProperty("created_at")
    private Instant createdAt;
}

