package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    private final ReportService reportService;

    /**
     * Exportar todas as entradas financeiras do condomínio em CSV
     * GET /condominiums/{condominiumId}/reports/financial.csv
     */
    @GetMapping("/financial.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportFinancialEntriesCSV(
            @PathVariable UUID condominiumId) {
        
        String csvContent = reportService.exportFinancialEntriesCSV(condominiumId);
        
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"relatorio-financeiro-" + LocalDate.now() + ".csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .body(csvContent);
    }

    /**
     * Exportar entradas financeiras filtradas por período
     * GET /condominiums/{condominiumId}/reports/financial.csv?startDate=2026-01-01&endDate=2026-05-31
     */
    @GetMapping("/financial/period.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportFinancialEntriesByPeriodCSV(
            @PathVariable UUID condominiumId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("dataInício deve ser antes da dataFim");
        }
        
        String csvContent = reportService.exportFinancialEntriesCSVFiltered(condominiumId, startDate, endDate);
        
        String filename = String.format("relatorio-financeiro-%s-ate-%s.csv", startDate, endDate);
        
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .body(csvContent);
    }

    /**
     * Exportar apenas entradas (INCOME)
     * GET /condominiums/{condominiumId}/reports/financial/income.csv
     */
    @GetMapping("/financial/income.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportIncomeEntriesCSV(
            @PathVariable UUID condominiumId) {
        
        String csvContent = reportService.exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.INCOME);
        
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"relatorio-entradas-" + LocalDate.now() + ".csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .body(csvContent);
    }

    /**
     * Exportar apenas saídas (EXPENSE)
     * GET /condominiums/{condominiumId}/reports/financial/expense.csv
     */
    @GetMapping("/financial/expense.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportExpenseEntriesCSV(
            @PathVariable UUID condominiumId) {
        
        String csvContent = reportService.exportFinancialEntriesCSVByType(condominiumId, FinancialEntryType.EXPENSE);
        
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"relatorio-saidas-" + LocalDate.now() + ".csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .body(csvContent);
    }

    /**
     * Exportar relatório do mês atual
     * GET /condominiums/{condominiumId}/reports/financial/month.csv?month=2026-05
     */
    @GetMapping("/financial/month.csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<String> exportFinancialEntriesByMonthCSV(
            @PathVariable UUID condominiumId,
            @RequestParam(value = "month", required = false) String monthStr) {
        
        YearMonth yearMonth;
        if (monthStr != null && !monthStr.isEmpty()) {
            try {
                yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Formato de mês inválido. Use: yyyy-MM");
            }
        } else {
            yearMonth = YearMonth.now();
        }
        
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        String csvContent = reportService.exportFinancialEntriesCSVFiltered(condominiumId, startDate, endDate);
        
        String filename = String.format("relatorio-financeiro-%s.csv", yearMonth);
        
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .body(csvContent);
    }
}

