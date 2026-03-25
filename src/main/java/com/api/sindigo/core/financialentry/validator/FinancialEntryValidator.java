package com.api.sindigo.core.financialentry.validator;

import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.validator.BaseValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FinancialEntryValidator extends BaseValidator {

    public void validateFinancialEntryCreation(FinancialEntryCreateDTO dto) {
        validateStringNotEmpty(dto.getDescription(), "Descrição");
        validateAmount(dto.getAmount());
    }

    public void validateAmount(BigDecimal amount) {
        validateCondition(
            amount != null && amount.compareTo(BigDecimal.ZERO) > 0,
            "Valor deve ser maior que 0"
        );
    }
}

