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
            !startDate.isAfter(endDate),
            "Data de início não pode ser após a data de fim"
        );
    }
}

