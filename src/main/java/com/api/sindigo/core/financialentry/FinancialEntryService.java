package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.financialentry.dto.BalanceResponseDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryResponseDTO;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import com.api.sindigo.core.financialentry.validator.FinancialEntryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialEntryService {

    private final FinancialEntryRepository financialEntryRepository;
    private final CondominiumRepository condominiumRepository;
    private final FinancialEntryDtoMapper financialEntryDtoMapper;
    private final FinancialEntryValidator financialEntryValidator;

    @Transactional
    public FinancialEntryResponseDTO addFinancialEntry(UUID condominiumId, FinancialEntryCreateDTO dto) {
        financialEntryValidator.validateFinancialEntryCreation(dto);

        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found with id: " + condominiumId));

        FinancialEntry financialEntry = new FinancialEntry();
        financialEntry.setType(dto.getType());
        financialEntry.setAmount(dto.getAmount());
        financialEntry.setDate(dto.getDate());
        financialEntry.setDescription(dto.getDescription());
        financialEntry.setCondominium(condominium);

        FinancialEntry saved = financialEntryRepository.save(financialEntry);

        return financialEntryDtoMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<FinancialEntryResponseDTO> listByCondominium(UUID condominiumId) {
        return financialEntryRepository.findByCondominiumId(condominiumId)
                .stream()
                .map(financialEntryDtoMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public BalanceResponseDTO getBalance(UUID condominiumId) {
        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found with id: " + condominiumId));

        BigDecimal totalIncome = financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.INCOME);

        BigDecimal totalExpense = financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.EXPENSE);

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        return BalanceResponseDTO.builder()
                .condominiumId(condominiumId)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .build();
    }
}
