package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.financialentry.dto.BalanceResponseDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryResponseDTO;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import com.api.sindigo.core.financialentry.validator.FinancialEntryValidator;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinancialEntryServiceTest {

    private final FinancialEntryRepository financialEntryRepository = mock(FinancialEntryRepository.class);
    private final CondominiumRepository condominiumRepository = mock(CondominiumRepository.class);
    private final FinancialEntryDtoMapper financialEntryDtoMapper = new FinancialEntryDtoMapper();
    private final FinancialEntryValidator financialEntryValidator = new FinancialEntryValidator();
    private final FinancialEntryService financialEntryService = new FinancialEntryService(
            financialEntryRepository,
            condominiumRepository,
            financialEntryDtoMapper,
            financialEntryValidator
    );

    private UUID condominiumId;
    private Condominium condominium;

    @BeforeEach
    void setUp() {
        condominiumId = UUID.randomUUID();
        condominium = Condominium.builder()
                .id(condominiumId)
                .owner(User.builder().id(UUID.randomUUID()).build())
                .name("Residencial Alfa")
                .build();
    }

    @Test
    void addFinancialEntryPersistsAndMapsResponse() {
        FinancialEntryCreateDTO dto = FinancialEntryCreateDTO.builder()
                .type(FinancialEntryType.INCOME)
                .amount(new BigDecimal("150.75"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Taxa de condomínio")
                .build();

        when(condominiumRepository.findById(condominiumId)).thenReturn(Optional.of(condominium));
        when(financialEntryRepository.save(any(FinancialEntry.class))).thenAnswer(invocation -> {
            FinancialEntry entry = invocation.getArgument(0);
            entry.setId(UUID.randomUUID());
            entry.setCreatedAt(LocalDate.of(2026, 6, 1));
            return entry;
        });

        FinancialEntryResponseDTO response = financialEntryService.addFinancialEntry(condominiumId, dto);

        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals(FinancialEntryType.INCOME, response.getType());
        assertEquals(new BigDecimal("150.75"), response.getAmount());
        verify(financialEntryRepository).save(any(FinancialEntry.class));
    }

    @Test
    void listByCondominiumReturnsMappedEntries() {
        FinancialEntry entry = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .type(FinancialEntryType.EXPENSE)
                .amount(new BigDecimal("80.00"))
                .date(LocalDate.of(2026, 6, 2))
                .description("Conta de luz")
                .condominium(condominium)
                .createdAt(LocalDate.of(2026, 6, 2))
                .build();

        when(financialEntryRepository.findByCondominiumId(condominiumId)).thenReturn(List.of(entry));

        List<FinancialEntryResponseDTO> response = financialEntryService.listByCondominium(condominiumId);

        assertEquals(1, response.size());
        assertEquals(entry.getId(), response.getFirst().getId());
        assertEquals("Conta de luz", response.getFirst().getDescription());
    }

    @Test
    void getBalanceCalculatesIncomeExpenseAndNetBalance() {
        when(financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.INCOME))
                .thenReturn(new BigDecimal("500.00"));
        when(financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.EXPENSE))
                .thenReturn(new BigDecimal("125.00"));

        BalanceResponseDTO response = financialEntryService.getBalance(condominiumId);

        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals(new BigDecimal("500.00"), response.getTotalIncome());
        assertEquals(new BigDecimal("125.00"), response.getTotalExpense());
        assertEquals(new BigDecimal("375.00"), response.getNetBalance());
    }

    @Test
    void addFinancialEntryRejectsMissingCondominium() {
        FinancialEntryCreateDTO dto = FinancialEntryCreateDTO.builder()
                .type(FinancialEntryType.EXPENSE)
                .amount(new BigDecimal("25.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Conta de água")
                .build();

        when(condominiumRepository.findById(condominiumId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> financialEntryService.addFinancialEntry(condominiumId, dto));
    }
}

