package com.api.sindigo.core.financialentry.validator;

import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialEntryValidatorTest {

    private final FinancialEntryValidator validator = new FinancialEntryValidator();

    @Test
    void validateFinancialEntryCreationAcceptsValidPayload() {
        FinancialEntryCreateDTO dto = FinancialEntryCreateDTO.builder()
                .type(FinancialEntryType.INCOME)
                .amount(new BigDecimal("150.75"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Taxa de condomínio")
                .build();

        assertDoesNotThrow(() -> validator.validateFinancialEntryCreation(dto));
    }

    @Test
    void validateFinancialEntryCreationRejectsEmptyDescription() {
        FinancialEntryCreateDTO dto = FinancialEntryCreateDTO.builder()
                .type(FinancialEntryType.EXPENSE)
                .amount(new BigDecimal("25.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description(" ")
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> validator.validateFinancialEntryCreation(dto));

        assertEquals("Descrição não pode ser vazio ou nulo", exception.getMessage());
    }

    @Test
    void validateAmountRejectsNonPositiveValues() {
        ValidationException exception = assertThrows(ValidationException.class, () -> validator.validateAmount(BigDecimal.ZERO));

        assertEquals("Valor deve ser maior que 0", exception.getMessage());
    }
}

