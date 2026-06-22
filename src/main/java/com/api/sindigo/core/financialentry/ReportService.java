package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final FinancialEntryRepository financialEntryRepository;

    @Transactional(readOnly = true)
    public String exportFinancialEntriesCSV(UUID condominiumId) {
        try {
            List<FinancialEntry> entries = financialEntryRepository.findByCondominiumIdOrderByDateDesc(condominiumId);
            return generateFinancialCSV(entries);
        } catch (IOException e) {
            log.error("Erro ao exportar relatório financeiro", e);
            throw new RuntimeException("Erro ao gerar relatório CSV: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public String exportFinancialEntriesCSVFiltered(UUID condominiumId, LocalDate startDate, LocalDate endDate) {
        try {
            List<FinancialEntry> entries = financialEntryRepository
                    .findByCondominiumIdAndDateBetweenOrderByDateDesc(condominiumId, startDate, endDate);
            return generateFinancialCSV(entries);
        } catch (IOException e) {
            log.error("Erro ao exportar relatório financeiro filtrado", e);
            throw new RuntimeException("Erro ao gerar relatório CSV: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public String exportFinancialEntriesCSVByType(UUID condominiumId, FinancialEntryType type) {
        try {
            List<FinancialEntry> entries = financialEntryRepository
                    .findByCondominiumIdAndTypeOrderByDateDesc(condominiumId, type);
            return generateFinancialCSV(entries);
        } catch (IOException e) {
            log.error("Erro ao exportar relatório financeiro por tipo", e);
            throw new RuntimeException("Erro ao gerar relatório CSV: " + e.getMessage(), e);
        }
    }

    private String generateFinancialCSV(List<FinancialEntry> entries) throws IOException {
        StringWriter sw = new StringWriter();

        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withHeader("Id", "Data", "Tipo", "Valor (R$)", "Descricao")
                .withRecordSeparator('\n')
                .withQuoteMode(QuoteMode.NON_NUMERIC);

        try (CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;

            for (FinancialEntry entry : entries) {
                String tipo = entry.getType().equals(FinancialEntryType.INCOME) ? "Entrada" : "Saida";
                String valor = entry.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toString();
                String descricao = capitalize(entry.getDescription());
                
                printer.printRecord(
                        entry.getId().toString(),
                        entry.getDate().format(formatter),
                        tipo,
                        valor,
                        descricao
                );

                if (entry.getType().equals(FinancialEntryType.INCOME)) {
                    totalIncome = totalIncome.add(entry.getAmount());
                } else {
                    totalExpense = totalExpense.add(entry.getAmount());
                }
            }

            BigDecimal saldo = totalIncome.subtract(totalExpense);
            String totalIncomeStr = totalIncome.setScale(2, java.math.RoundingMode.HALF_UP).toString();
            String totalExpenseStr = totalExpense.setScale(2, java.math.RoundingMode.HALF_UP).toString();
            String saldoStr = saldo.setScale(2, java.math.RoundingMode.HALF_UP).toString();
            
            printer.printRecord("", "", "", "", "");
            printer.printRecord("Resumo", "", "", "", "");
            printer.printRecord("Total entradas", "", "", totalIncomeStr, "");
            printer.printRecord("Total saidas", "", "", totalExpenseStr, "");
            printer.printRecord("Saldo", "", "", saldoStr, "");
        }

        log.info("Relatório CSV gerado com sucesso. Total de registros: {}", entries.size());
        return sw.toString();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}

