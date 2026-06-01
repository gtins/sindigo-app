package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private final FinancialEntryRepository financialEntryRepository = mock(FinancialEntryRepository.class);
    private final ReportService reportService = new ReportService(financialEntryRepository);

    private UUID condominiumId;

    @BeforeEach
    void setUp() {
        condominiumId = UUID.randomUUID();
    }

    @Test
    void exportFinancialEntriesCSVGeneratesHeaderEntriesAndSummary() {
        FinancialEntry income = buildEntry(FinancialEntryType.INCOME, new BigDecimal("500.00"), LocalDate.of(2026, 6, 2), "taxa de condomínio");
        FinancialEntry expense = buildEntry(FinancialEntryType.EXPENSE, new BigDecimal("125.50"), LocalDate.of(2026, 6, 1), "conta de luz");

        when(financialEntryRepository.findByCondominiumIdOrderByDateDesc(condominiumId)).thenReturn(List.of(income, expense));

        String csv = reportService.exportFinancialEntriesCSV(condominiumId);

        assertTrue(csv.contains("Resumo"));
        assertTrue(csv.contains("Total entradas"));
        assertTrue(csv.contains("Total saidas"));
        assertTrue(csv.contains("Saldo"));
        assertTrue(csv.contains("500.00"));
        assertTrue(csv.contains("125.50"));
        assertTrue(csv.contains("Conta de luz"));
        verify(financialEntryRepository).findByCondominiumIdOrderByDateDesc(condominiumId);
    }

    @Test
    void exportFinancialEntriesCSVFilteredUsesDateRangeRepositoryMethod() {
        FinancialEntry income = buildEntry(FinancialEntryType.INCOME, new BigDecimal("150.00"), LocalDate.of(2026, 6, 3), "taxa extra");
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);

        when(financialEntryRepository.findByCondominiumIdAndDateBetweenOrderByDateDesc(condominiumId, startDate, endDate))
                .thenReturn(List.of(income));

        String csv = reportService.exportFinancialEntriesCSVFiltered(condominiumId, startDate, endDate);

        assertTrue(csv.contains("Taxa extra"));
        verify(financialEntryRepository).findByCondominiumIdAndDateBetweenOrderByDateDesc(condominiumId, startDate, endDate);
    }

    @Test
    void exportFinancialEntriesCSVByTypeUsesTypeRepositoryMethod() {
        FinancialEntry expense = buildEntry(FinancialEntryType.EXPENSE, new BigDecimal("75.00"), LocalDate.of(2026, 6, 4), "manutenção do portão");

        when(financialEntryRepository.findByCondominiumIdAndTypeOrderByDateDesc(condominiumId, FinancialEntryType.EXPENSE))
                .thenReturn(List.of(expense));

        String csv = reportService.exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.EXPENSE);

        assertTrue(csv.contains("Saida"));
        assertTrue(csv.contains("75.00"));
        verify(financialEntryRepository).findByCondominiumIdAndTypeOrderByDateDesc(condominiumId, FinancialEntryType.EXPENSE);
    }

    private FinancialEntry buildEntry(FinancialEntryType type, BigDecimal amount, LocalDate date, String description) {
        return FinancialEntry.builder()
                .id(UUID.randomUUID())
                .type(type)
                .amount(amount)
                .date(date)
                .description(description)
                .condominium(Condominium.builder().id(condominiumId).owner(User.builder().id(UUID.randomUUID()).build()).build())
                .build();
    }
}



