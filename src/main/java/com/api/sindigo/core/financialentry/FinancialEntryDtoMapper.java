package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryResponseDTO;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import org.springframework.stereotype.Component;

@Component
public class FinancialEntryDtoMapper {

    public FinancialEntryResponseDTO toResponseDTO(FinancialEntry financialEntry) {
        return FinancialEntryResponseDTO.builder()
                .id(financialEntry.getId())
                .condominiumId(financialEntry.getCondominium().getId())
                .type(financialEntry.getType())
                .amount(financialEntry.getAmount())
                .date(financialEntry.getDate())
                .description(financialEntry.getDescription())
                .createdAt(financialEntry.getCreatedAt())
                .build();
    }
}

