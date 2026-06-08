package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/condominiums/{condominiumId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final String CSV_EXTENSION = ".csv";
    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";
    private static final String ATTACHMENT_FILENAME_PREFIX = "attachment; filename=\"";
    private static final String ATTACHMENT_FILENAME_SUFFIX = "\"";

    private static final String FINANCIAL_REPORT_PREFIX = "relatorio-financeiro-";
    private static final String INCOME_REPORT_PREFIX = "relatorio-entradas-";
    private static final String EXPENSE_REPORT_PREFIX = "relatorio-saidas-";

    private static final String INVALID_DATE_RANGE_MESSAGE = "dataInício deve ser antes da dataFim";
    private static final String INVALID_MONTH_FORMAT_MESSAGE = "Formato de mês inválido. Use: yyyy-MM";

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ReportService reportService;

    @GetMapping("/financial.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportFinancialEntriesCSV(@PathVariable UUID condominiumId) {
        String csvContent = reportService.exportFinancialEntriesCSV(condominiumId);
        String filename = FINANCIAL_REPORT_PREFIX + LocalDate.now() + CSV_EXTENSION;

        return buildCsvResponse(filename, csvContent);
    }

    @GetMapping("/financial/period.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportFinancialEntriesByPeriodCSV(
            @PathVariable UUID condominiumId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(INVALID_DATE_RANGE_MESSAGE);
        }

        String csvContent = reportService.exportFinancialEntriesCSVFiltered(condominiumId, startDate, endDate);
        String filename = String.format("%s%s-ate-%s%s", FINANCIAL_REPORT_PREFIX, startDate, endDate, CSV_EXTENSION);

        return buildCsvResponse(filename, csvContent);
    }

    @GetMapping("/financial/income.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportIncomeEntriesCSV(@PathVariable UUID condominiumId) {
        String csvContent = reportService.exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.INCOME);
        String filename = INCOME_REPORT_PREFIX + LocalDate.now() + CSV_EXTENSION;

        return buildCsvResponse(filename, csvContent);
    }

    @GetMapping("/financial/expense.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportExpenseEntriesCSV(@PathVariable UUID condominiumId) {
        String csvContent = reportService.exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.EXPENSE);
        String filename = EXPENSE_REPORT_PREFIX + LocalDate.now() + CSV_EXTENSION;

        return buildCsvResponse(filename, csvContent);
    }

    @GetMapping("/financial/month.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportFinancialEntriesByMonthCSV(
            @PathVariable UUID condominiumId,
            @RequestParam(value = "month", required = false) String monthStr) {

        YearMonth yearMonth = resolveYearMonth(monthStr);

        if (yearMonth == null) {
            return ResponseEntity.badRequest().body(INVALID_MONTH_FORMAT_MESSAGE);
        }

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String csvContent = reportService.exportFinancialEntriesCSVFiltered(condominiumId, startDate, endDate);
        String filename = FINANCIAL_REPORT_PREFIX + yearMonth + CSV_EXTENSION;

        return buildCsvResponse(filename, csvContent);
    }

    private YearMonth resolveYearMonth(String monthStr) {
        if (monthStr == null || monthStr.isBlank()) {
            return YearMonth.now();
        }

        try {
            return YearMonth.parse(monthStr, YEAR_MONTH_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private ResponseEntity<String> buildCsvResponse(String filename, String csvContent) {
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(filename))
                .header(HttpHeaders.CONTENT_TYPE, CSV_CONTENT_TYPE)
                .body(csvContent);
    }

    private String buildContentDisposition(String filename) {
        return ATTACHMENT_FILENAME_PREFIX + filename + ATTACHMENT_FILENAME_SUFFIX;
    }
}