package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportControllerTest {

    private final ReportService reportService = mock(ReportService.class);
    private final ReportController controller = new ReportController(reportService);

    @Test
    void exportFinancialEntriesCsvReturnsAttachmentHeaders() {
        UUID condominiumId = UUID.randomUUID();
        when(reportService.exportFinancialEntriesCSV(condominiumId)).thenReturn("csv-content");

        var result = controller.exportFinancialEntriesCSV(condominiumId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("csv-content", result.getBody());
        assertEquals("attachment; filename=\"relatorio-financeiro-" + LocalDate.now() + ".csv\"", result.getHeaders().getFirst("Content-Disposition"));
        verify(reportService).exportFinancialEntriesCSV(condominiumId);
    }

    @Test
    void exportFinancialEntriesByPeriodCsvRejectsInvalidRange() {
        UUID condominiumId = UUID.randomUUID();

        var result = controller.exportFinancialEntriesByPeriodCSV(
                condominiumId,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 1)
        );

        assertEquals(400, result.getStatusCode().value());
        assertEquals("dataInício deve ser antes da dataFim", result.getBody());
    }

    @Test
    void exportFinancialEntriesByPeriodCsvReturnsAttachmentFilename() {
        UUID condominiumId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);
        when(reportService.exportFinancialEntriesCSVFiltered(condominiumId, startDate, endDate)).thenReturn("period-csv");

        var result = controller.exportFinancialEntriesByPeriodCSV(condominiumId, startDate, endDate);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("period-csv", result.getBody());
        assertEquals("attachment; filename=\"relatorio-financeiro-2026-06-01-ate-2026-06-30.csv\"", result.getHeaders().getFirst("Content-Disposition"));
    }

    @Test
    void exportIncomeEntriesCsvUsesIncomeType() {
        UUID condominiumId = UUID.randomUUID();
        when(reportService.exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.INCOME)).thenReturn("income-csv");

        var result = controller.exportIncomeEntriesCSV(condominiumId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("income-csv", result.getBody());
        verify(reportService).exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.INCOME);
    }

    @Test
    void exportExpenseEntriesCsvUsesExpenseType() {
        UUID condominiumId = UUID.randomUUID();
        when(reportService.exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.EXPENSE)).thenReturn("expense-csv");

        var result = controller.exportExpenseEntriesCSV(condominiumId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("expense-csv", result.getBody());
        verify(reportService).exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.EXPENSE);
    }

    @Test
    void exportFinancialEntriesByMonthCsvRejectsInvalidMonthFormat() {
        UUID condominiumId = UUID.randomUUID();

        var result = controller.exportFinancialEntriesByMonthCSV(condominiumId, "2026/06");

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Formato de mês inválido. Use: yyyy-MM", result.getBody());
    }

    @Test
    void exportFinancialEntriesByMonthCsvUsesProvidedMonth() {
        UUID condominiumId = UUID.randomUUID();
        when(reportService.exportFinancialEntriesCSVFiltered(eq(condominiumId), any(), any())).thenReturn("month-csv");

        var result = controller.exportFinancialEntriesByMonthCSV(condominiumId, "2026-06");

        assertEquals(200, result.getStatusCode().value());
        assertEquals("month-csv", result.getBody());
        assertEquals("attachment; filename=\"relatorio-financeiro-2026-06.csv\"", result.getHeaders().getFirst("Content-Disposition"));
    }
}


