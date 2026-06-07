package com.api.sindigo.core.reservation.validator;

import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class ReservationValidatorTest {

    private static final LocalDateTime TEST_REFERENCE_DATE =
            LocalDateTime.of(2099, Month.DECEMBER, 25, 12, 0, 0);

    private final ReservationValidator validator = new ReservationValidator();

    @Test
    void validateReservationCreationAcceptsValidPayload() {
        LocalDateTime validStart = TEST_REFERENCE_DATE
                .withHour(18)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime validEnd = validStart.plusHours(4);

        ReservationCreateDTO dto = buildReservationCreateDTO(validStart, validEnd);

        assertDoesNotThrow(() -> validator.validateReservationCreation(dto));
    }

    @Test
    void validateReservationCreationRejectsInvalidTimeRange() {
        LocalDateTime baseDate = TEST_REFERENCE_DATE
                .withHour(18)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime invalidStart = baseDate.plusHours(2);
        LocalDateTime invalidEnd = baseDate;

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTimeRange(invalidStart, invalidEnd)
        );

        assertEquals("Hora de início deve ser antes da hora de fim", exception.getMessage());
    }

    @Test
    void validateReservationCreationRejectsInsufficientAdvanceNotice() {
        LocalDateTime mockedNow = LocalDateTime.of(2026, Month.JUNE, 10, 10, 0, 0);

        LocalDateTime invalidStart = mockedNow
                .plusDays(3)
                .withHour(18)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime invalidEnd = invalidStart.plusHours(4);

        ReservationCreateDTO dto = buildReservationCreateDTO(invalidStart, invalidEnd);

        try (MockedStatic<LocalDateTime> mockedDateTime =
                     mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {

            mockedDateTime.when(LocalDateTime::now).thenReturn(mockedNow);

            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> validator.validateReservationCreation(dto)
            );

            assertEquals("As reservas devem ser feitas com no mínimo 7 dias de antecedência", exception.getMessage());
        }
    }

    @Test
    void validateReservationCreationRejectsExcessiveDuration() {
        LocalDateTime validStart = TEST_REFERENCE_DATE
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime excessiveEnd = validStart.plusHours(8);

        ReservationCreateDTO dto = buildReservationCreateDTO(validStart, excessiveEnd);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateReservationCreation(dto)
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

    private ReservationCreateDTO buildReservationCreateDTO(LocalDateTime startTime, LocalDateTime endTime) {
        return ReservationCreateDTO.builder()
                .area("Salão de festas")
                .unitNumber("201")
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}