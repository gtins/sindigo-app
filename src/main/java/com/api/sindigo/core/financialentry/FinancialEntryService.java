package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryResponseDTO;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialEntryService {

    private final FinancialEntryRepository financialEntryRepository;
    private final CondominiumRepository condominiumRepository;
    private final FinancialEntryDtoMapper financialEntryDtoMapper;

    // CREATE - Para endpoint POST /condominiums/{id}/financial-entries
    public FinancialEntryResponseDTO addFinancialEntry(UUID condominiumId, FinancialEntryCreateDTO dto) {
        // Buscar condomínio
        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found with id: " + condominiumId));

        // Validação: amount > 0
        if (dto.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // Vincular entidade
        FinancialEntry financialEntry = new FinancialEntry();
        financialEntry.setType(dto.getType());
        financialEntry.setAmount(dto.getAmount());
        financialEntry.setDate(dto.getDate());
        financialEntry.setDescription(dto.getDescription());
        financialEntry.setCondominium(condominium);

        // Salvar entrada financeira
        FinancialEntry saved = financialEntryRepository.save(financialEntry);

        return financialEntryDtoMapper.toResponseDTO(saved);
    }

    // LIST
    public List<FinancialEntryResponseDTO> listByCondominium(UUID condominiumId) {
        return financialEntryRepository.findByCondominiumId(condominiumId)
                .stream()
                .map(financialEntryDtoMapper::toResponseDTO)
                .toList();
    }
}
