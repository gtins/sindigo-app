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
        // Valid date: 8 days from now
        LocalDateTime validStart = LocalDateTime.now().plusDays(8).withHour(18).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime validEnd = validStart.plusHours(4);
        
        ReservationCreateDTO dto = ReservationCreateDTO.builder()
                .area("Salão de festas")
                .unitNumber("201")
                .startTime(validStart)
                .endTime(validEnd)
                .build();

        assertDoesNotThrow(() -> validator.validateReservationCreation(dto));
    }

    @Test
    void validateReservationCreationRejectsInvalidTimeRange() {
        // Use a date 8 days from now, but with invalid time range (end before start)
        LocalDateTime baseDate = LocalDateTime.now().plusDays(8).withHour(18).withMinute(0).withSecond(0).withNano(0);
        
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTimeRange(
                        baseDate.plusHours(2),
                        baseDate
                )
        );

        assertEquals("Hora de início deve ser antes da hora de fim", exception.getMessage());
    }

    @Test
    void validateReservationCreationRejectsInsufficientAdvanceNotice() {
        // Invalid date: only 3 days from now (less than 7 days required)
        LocalDateTime invalidStart = LocalDateTime.now().plusDays(3).withHour(18).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime invalidEnd = invalidStart.plusHours(4);
        
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateReservationCreation(
                        ReservationCreateDTO.builder()
                                .area("Salão de festas")
                                .unitNumber("201")
                                .startTime(invalidStart)
                                .endTime(invalidEnd)
                                .build()
                )
        );

        assertEquals("As reservas devem ser feitas com no mínimo 7 dias de antecedência", exception.getMessage());
    }

    @Test
    void validateReservationCreationRejectsExcessiveDuration() {
        // Valid date but excessive duration (8 hours, max is 6)
        LocalDateTime validStart = LocalDateTime.now().plusDays(8).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime excessiveEnd = validStart.plusHours(8);
        
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateReservationCreation(
                        ReservationCreateDTO.builder()
                                .area("Salão de festas")
                                .unitNumber("201")
                                .startTime(validStart)
                                .endTime(excessiveEnd)
                                .build()
                )
        );

        assertEquals("O período máximo permitido para uma reserva é de 6 horas", exception.getMessage());
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

