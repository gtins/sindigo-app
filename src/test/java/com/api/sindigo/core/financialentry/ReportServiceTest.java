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

    @Test
    void exportFinancialEntriesCSVByTypeIncomeFilters() {
        FinancialEntry income = buildEntry(FinancialEntryType.INCOME, new BigDecimal("300.00"), LocalDate.of(2026, 6, 5), "doacao");

        when(financialEntryRepository.findByCondominiumIdAndTypeOrderByDateDesc(condominiumId, FinancialEntryType.INCOME))
                .thenReturn(List.of(income));

        String csv = reportService.exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.INCOME);

        assertTrue(csv.contains("Entrada"));
        assertTrue(csv.contains("300.00"));
        assertTrue(csv.contains("Doacao"));
    }

    @Test
    void exportFinancialEntriesCSVWithLargeAmounts() {
        FinancialEntry largeIncome = buildEntry(FinancialEntryType.INCOME, new BigDecimal("9999.99"), LocalDate.of(2026, 6, 1), "grande arrecadação");
        FinancialEntry largeExpense = buildEntry(FinancialEntryType.EXPENSE, new BigDecimal("5555.55"), LocalDate.of(2026, 6, 2), "investimento grande");

        when(financialEntryRepository.findByCondominiumIdOrderByDateDesc(condominiumId))
                .thenReturn(List.of(largeIncome, largeExpense));

        String csv = reportService.exportFinancialEntriesCSV(condominiumId);

        assertTrue(csv.contains("9999.99"));
        assertTrue(csv.contains("5555.55"));
        assertTrue(csv.contains("4444.44")); // balance: 9999.99 - 5555.55
    }

    @Test
    void exportFinancialEntriesCSVWithEmptyList() {
        when(financialEntryRepository.findByCondominiumIdOrderByDateDesc(condominiumId))
                .thenReturn(List.of());

        String csv = reportService.exportFinancialEntriesCSV(condominiumId);

        assertTrue(csv.contains("Resumo"));
        assertTrue(csv.contains("0.00"));
    }

    @Test
    void exportFinancialEntriesCSVFormatsDateCorrectly() {
        FinancialEntry entry = buildEntry(FinancialEntryType.INCOME, new BigDecimal("100.00"), LocalDate.of(2026, 12, 25), "teste data");

        when(financialEntryRepository.findByCondominiumIdOrderByDateDesc(condominiumId))
                .thenReturn(List.of(entry));

        String csv = reportService.exportFinancialEntriesCSV(condominiumId);

        assertTrue(csv.contains("25/12/2026"));
    }

    @Test
    void exportFinancialEntriesCSVCapitalizesDescriptions() {
        FinancialEntry entry = buildEntry(FinancialEntryType.INCOME, new BigDecimal("100.00"), LocalDate.of(2026, 6, 1), "DESCRICAO EM MAIUSCULA");

        when(financialEntryRepository.findByCondominiumIdOrderByDateDesc(condominiumId))
                .thenReturn(List.of(entry));

        String csv = reportService.exportFinancialEntriesCSV(condominiumId);

        assertTrue(csv.contains("Descricao em maiuscula"));
    }

    @Test
    void exportFinancialEntriesCSVCalculatesBalanceCorrectly() {
        FinancialEntry income1 = buildEntry(FinancialEntryType.INCOME, new BigDecimal("1000.00"), LocalDate.of(2026, 6, 1), "entrada 1");
        FinancialEntry income2 = buildEntry(FinancialEntryType.INCOME, new BigDecimal("500.00"), LocalDate.of(2026, 6, 2), "entrada 2");
        FinancialEntry expense1 = buildEntry(FinancialEntryType.EXPENSE, new BigDecimal("200.00"), LocalDate.of(2026, 6, 3), "saida 1");
        FinancialEntry expense2 = buildEntry(FinancialEntryType.EXPENSE, new BigDecimal("150.00"), LocalDate.of(2026, 6, 4), "saida 2");

        when(financialEntryRepository.findByCondominiumIdOrderByDateDesc(condominiumId))
                .thenReturn(List.of(income1, income2, expense1, expense2));

        String csv = reportService.exportFinancialEntriesCSV(condominiumId);

        // Total income: 1500, total expense: 350, balance: 1150
        assertTrue(csv.contains("1500.00"));
        assertTrue(csv.contains("350.00"));
        assertTrue(csv.contains("1150.00"));
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



