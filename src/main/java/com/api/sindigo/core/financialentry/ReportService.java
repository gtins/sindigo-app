package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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

    /**
     * Exportar todas as entradas financeiras do condomínio para CSV
     */
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

    /**
     * Exportar entradas financeiras filtradas por período
     */
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

    /**
     * Exportar entradas financeiras filtradas por tipo (ENTRADA ou SAÍDA)
     */
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

    /**
     * Gera o CSV formatado com cabeçalhos e dados
     */
    private String generateFinancialCSV(List<FinancialEntry> entries) throws IOException {
        StringWriter sw = new StringWriter();
        
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withHeader("ID", "Data", "Tipo", "Valor", "Descrição")
                .withRecordSeparator('\n');

        try (CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;

            // Adicionar cada entrada ao CSV
            for (FinancialEntry entry : entries) {
                printer.printRecord(
                        entry.getId().toString(),
                        entry.getDate().format(formatter),
                        entry.getType().equals(FinancialEntryType.INCOME) ? "ENTRADA" : "SAÍDA",
                        "R$ " + entry.getAmount().setScale(2, java.math.RoundingMode.HALF_UP),
                        entry.getDescription()
                );

                // Calcular totais
                if (entry.getType().equals(FinancialEntryType.INCOME)) {
                    totalIncome = totalIncome.add(entry.getAmount());
                } else {
                    totalExpense = totalExpense.add(entry.getAmount());
                }
            }

            // Adicionar linha em branco para separação
            printer.println();

            // Adicionar resumo
            printer.printRecord("", "", "RESUMO", "", "");
            printer.printRecord("", "Total Entradas:", "", "R$ " + totalIncome.setScale(2, java.math.RoundingMode.HALF_UP), "");
            printer.printRecord("", "Total Saídas:", "", "R$ " + totalExpense.setScale(2, java.math.RoundingMode.HALF_UP), "");
            printer.printRecord("", "Saldo:", "", "R$ " + totalIncome.subtract(totalExpense).setScale(2, java.math.RoundingMode.HALF_UP), "");
        }

        log.info("Relatório CSV gerado com sucesso. Total de registros: {}", entries.size());
        return sw.toString();
    }
}

