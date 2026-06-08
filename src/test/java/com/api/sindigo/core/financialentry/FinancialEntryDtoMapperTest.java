package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.financialentry.dto.FinancialEntryResponseDTO;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinancialEntryDtoMapperTest {

    private final FinancialEntryDtoMapper mapper = new FinancialEntryDtoMapper();

    @Test
    void toResponseDTOMapsIncomeEntryWithAllFields() {
        UUID financialEntryId = UUID.randomUUID();
        UUID condominiumId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1500.50");
        LocalDate date = LocalDate.of(2026, 6, 1);
        LocalDate createdAt = LocalDate.of(2026, 6, 1);

        Condominium condominium = Condominium.builder()
                .id(condominiumId)
                .name("Condomínio Alfa")
                .build();

        FinancialEntry entry = FinancialEntry.builder()
                .id(financialEntryId)
                .condominium(condominium)
                .type(FinancialEntryType.INCOME)
                .amount(amount)
                .date(date)
                .description("Recebimento de taxa condominial")
                .createdAt(createdAt)
                .build();

        FinancialEntryResponseDTO dto = mapper.toResponseDTO(entry);

        assertNotNull(dto);
        assertEquals(financialEntryId, dto.getId());
        assertEquals(condominiumId, dto.getCondominiumId());
        assertEquals(FinancialEntryType.INCOME, dto.getType());
        assertEquals(amount, dto.getAmount());
        assertEquals(date, dto.getDate());
        assertEquals("Recebimento de taxa condominial", dto.getDescription());
    }

    @Test
    void toResponseDTOMapsExpenseEntryWithAllFields() {
        UUID financialEntryId = UUID.randomUUID();
        UUID condominiumId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("250.75");
        LocalDate date = LocalDate.of(2026, 6, 5);
        LocalDate createdAt = LocalDate.of(2026, 6, 5);

        Condominium condominium = Condominium.builder()
                .id(condominiumId)
                .name("Residencial Beta")
                .build();

        FinancialEntry entry = FinancialEntry.builder()
                .id(financialEntryId)
                .condominium(condominium)
                .type(FinancialEntryType.EXPENSE)
                .amount(amount)
                .date(date)
                .description("Pagamento de luz")
                .createdAt(createdAt)
                .build();

        FinancialEntryResponseDTO dto = mapper.toResponseDTO(entry);

        assertNotNull(dto);
        assertEquals(financialEntryId, dto.getId());
        assertEquals(condominiumId, dto.getCondominiumId());
        assertEquals(FinancialEntryType.EXPENSE, dto.getType());
        assertEquals(amount, dto.getAmount());
        assertEquals(date, dto.getDate());
        assertEquals("Pagamento de luz", dto.getDescription());
    }

    @Test
    void toResponseDTOMapsEntryWithSmallAmount() {
        UUID condominiumId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("0.01");

        Condominium condominium = Condominium.builder()
                .id(condominiumId)
                .build();

        FinancialEntry entry = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .type(FinancialEntryType.INCOME)
                .amount(amount)
                .date(LocalDate.now())
                .description("Ajuste")
                .createdAt(LocalDate.now())
                .build();

        FinancialEntryResponseDTO dto = mapper.toResponseDTO(entry);

        assertNotNull(dto);
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void toResponseDTOMapsEntryWithLargeAmount() {
        UUID condominiumId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("999999.99");

        Condominium condominium = Condominium.builder()
                .id(condominiumId)
                .build();

        FinancialEntry entry = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .type(FinancialEntryType.INCOME)
                .amount(amount)
                .date(LocalDate.now())
                .description("Grande arrecadação")
                .createdAt(LocalDate.now())
                .build();

        FinancialEntryResponseDTO dto = mapper.toResponseDTO(entry);

        assertNotNull(dto);
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void toResponseDTOMapsEntryWithLongDescription() {
        UUID condominiumId = UUID.randomUUID();
        String longDescription = "Esta é uma descrição muito longa que contém vários detalhes sobre a entrada " +
                                  "financeira, incluindo informações sobre a transação, referência fiscal, " +
                                  "e outras observações importantes para auditoria e controle.";

        Condominium condominium = Condominium.builder()
                .id(condominiumId)
                .build();

        FinancialEntry entry = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .type(FinancialEntryType.INCOME)
                .amount(new BigDecimal("1000.00"))
                .date(LocalDate.now())
                .description(longDescription)
                .createdAt(LocalDate.now())
                .build();

        FinancialEntryResponseDTO dto = mapper.toResponseDTO(entry);

        assertNotNull(dto);
        assertEquals(longDescription, dto.getDescription());
    }

    @Test
    void toResponseDTOMapsMultipleEntriesDifferently() {
        UUID condominiumId = UUID.randomUUID();
        Condominium condominium = Condominium.builder().id(condominiumId).build();

        FinancialEntry income = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .type(FinancialEntryType.INCOME)
                .amount(new BigDecimal("1000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Entrada")
                .createdAt(LocalDate.now())
                .build();

        FinancialEntry expense = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .type(FinancialEntryType.EXPENSE)
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.of(2026, 6, 2))
                .description("Saída")
                .createdAt(LocalDate.now())
                .build();

        FinancialEntryResponseDTO incomeDTO = mapper.toResponseDTO(income);
        FinancialEntryResponseDTO expenseDTO = mapper.toResponseDTO(expense);

        assertNotNull(incomeDTO);
        assertNotNull(expenseDTO);
        assertEquals(FinancialEntryType.INCOME, incomeDTO.getType());
        assertEquals(FinancialEntryType.EXPENSE, expenseDTO.getType());
        assertEquals(new BigDecimal("1000.00"), incomeDTO.getAmount());
        assertEquals(new BigDecimal("200.00"), expenseDTO.getAmount());
    }
}




