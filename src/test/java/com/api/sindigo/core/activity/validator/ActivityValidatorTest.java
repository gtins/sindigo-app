package com.api.sindigo.core.activity.validator;

import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.entities.ActivityType;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivityValidatorTest {

    private final ActivityValidator validator = new ActivityValidator();

    @Test
    void validateActivityCreationAcceptsValidPayload() {
        ActivityCreateDTO dto = new ActivityCreateDTO(
                "Reunião",
                "Reunião ordinária do condomínio",
                ActivityType.ONCE,
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 2),
                null,
                null
        );

        assertDoesNotThrow(() -> validator.validateActivityCreation(dto));
    }

    @Test
    void validateDateRangeRejectsInvertedRange() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateDateRange(
                        LocalDate.of(2026, 6, 2),
                        LocalDate.of(2026, 6, 1)
                )
        );

        assertEquals("Data de início não pode ser após a data de fim", exception.getMessage());
    }

    @Test
    void validateDateRangeAcceptsSameDateForStartAndEnd() {
        assertDoesNotThrow(() -> validator.validateDateRange(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 1)
        ));
    }
}


