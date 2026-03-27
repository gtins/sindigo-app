package com.api.sindigo.core.activity.validator;

import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.validator.BaseValidator;
import org.springframework.stereotype.Component;

@Component
public class ActivityValidator extends BaseValidator {

    public void validateActivityCreation(ActivityCreateDTO dto) {
        validateStringNotEmpty(dto.getTitle(), "Título");
        validateStringNotEmpty(dto.getDescription(), "Descrição");
        validateDateRange(dto.getStartDate(), dto.getEndDate());
    }

    public void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        validateCondition(
            startDate.isBefore(endDate),
            "Data de início deve ser antes da data de fim"
        );
    }
}

