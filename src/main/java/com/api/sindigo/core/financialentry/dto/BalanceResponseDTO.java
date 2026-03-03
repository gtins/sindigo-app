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

    @JsonProperty("condominium_id")
    private UUID condominiumId;

    @JsonProperty("total_income")
    private BigDecimal totalIncome;

    @JsonProperty("total_expense")
    private BigDecimal totalExpense;

    @JsonProperty("net_balance")
    private BigDecimal netBalance;
}

