package com.api.sindigo.core.financialentry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponseDTO {

    private UUID condominiumId;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netBalance;
}

