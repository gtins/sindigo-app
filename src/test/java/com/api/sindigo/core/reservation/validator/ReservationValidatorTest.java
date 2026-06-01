package com.api.sindigo.core.reservation.validator;

import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationValidatorTest {

    private final ReservationValidator validator = new ReservationValidator();

    @Test
    void validateReservationCreationAcceptsValidPayload() {
        ReservationCreateDTO dto = ReservationCreateDTO.builder()
                .area("Salão de festas")
                .unitNumber("201")
                .startTime(LocalDateTime.of(2026, 6, 1, 18, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 22, 0))
                .build();

        assertDoesNotThrow(() -> validator.validateReservationCreation(dto));
    }

    @Test
    void validateReservationCreationRejectsInvalidTimeRange() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTimeRange(
                        LocalDateTime.of(2026, 6, 1, 22, 0),
                        LocalDateTime.of(2026, 6, 1, 18, 0)
                )
        );

        assertEquals("Hora de início deve ser antes da hora de fim", exception.getMessage());
    }

    @Test
    void validateNoConflictsRejectsExistingConflicts() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateNoConflicts(true)
        );

        assertEquals("Já existe uma reserva para esta área no período solicitado", exception.getMessage());
    }
}

