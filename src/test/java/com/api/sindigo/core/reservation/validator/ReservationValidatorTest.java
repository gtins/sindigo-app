package com.api.sindigo.core.reservation.validator;

import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        ZonedDateTime mockedZonedNow = mockedNow.atZone(ZoneId.systemDefault());

        LocalDateTime invalidStart = mockedNow
                .plusHours(12)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime invalidEnd = invalidStart.plusHours(4);

        ReservationCreateDTO dto = buildReservationCreateDTO(invalidStart, invalidEnd);

        try (MockedStatic<ZonedDateTime> mockedZDT = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            mockedZDT
                    .when(() -> ZonedDateTime.now(any(ZoneId.class)))
                    .thenReturn(mockedZonedNow);

            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> validator.validateReservationCreation(dto)
            );

            assertEquals("As reservas devem ser feitas com no mínimo 24 horas de antecedência", exception.getMessage());
        }
    }

    @Test
    void validateReservationCreationRejectsExcessiveDuration() {
        LocalDateTime validStart = TEST_REFERENCE_DATE
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime excessiveEnd = validStart.plusHours(26);

        ReservationCreateDTO dto = buildReservationCreateDTO(validStart, excessiveEnd);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateReservationCreation(dto)
        );

        assertEquals("O período máximo permitido para uma reserva é de 24 horas", exception.getMessage());
    }

    @Test
    void validateNoConflictsRejectsExistingConflicts() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateNoConflicts(true)
        );

        assertEquals("Já existe uma reserva para esta área no período solicitado", exception.getMessage());
    }

    @Test
    void validateCancellationAcceptsValidCancellation() {
        LocalDateTime mockedNow = LocalDateTime.of(2026, Month.JUNE, 10, 10, 0, 0);
        ZonedDateTime mockedZonedNow = mockedNow.atZone(ZoneId.systemDefault());

        LocalDateTime validStart = mockedNow
                .plusHours(25)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        try (MockedStatic<ZonedDateTime> mockedZDT = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            mockedZDT
                    .when(() -> ZonedDateTime.now(any(ZoneId.class)))
                    .thenReturn(mockedZonedNow);

            assertDoesNotThrow(() -> validator.validateCancellation(validStart));
        }
    }

    @Test
    void validateCancellationRejectsInsufficientAdvanceNotice() {
        LocalDateTime mockedNow = LocalDateTime.of(2026, Month.JUNE, 10, 10, 0, 0);
        ZonedDateTime mockedZonedNow = mockedNow.atZone(ZoneId.systemDefault());

        LocalDateTime invalidStart = mockedNow
                .plusHours(12)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        try (MockedStatic<ZonedDateTime> mockedZDT = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            mockedZDT
                    .when(() -> ZonedDateTime.now(any(ZoneId.class)))
                    .thenReturn(mockedZonedNow);

            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> validator.validateCancellation(invalidStart)
            );

            assertEquals("Reservas só podem ser canceladas com no mínimo 24 horas de antecedência", exception.getMessage());
        }
    }

    @Test
    void validateReservationCreationAcceptsMinimumAdvanceNotice() {
        LocalDateTime mockedNow = LocalDateTime.of(2026, Month.JUNE, 10, 10, 0, 0);
        ZonedDateTime mockedZonedNow = mockedNow.atZone(ZoneId.systemDefault());

        LocalDateTime validStart = mockedNow
                .plusHours(24)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime validEnd = validStart.plusHours(4);

        ReservationCreateDTO dto = buildReservationCreateDTO(validStart, validEnd);

        try (MockedStatic<ZonedDateTime> mockedZDT = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            mockedZDT
                    .when(() -> ZonedDateTime.now(any(ZoneId.class)))
                    .thenReturn(mockedZonedNow);

            assertDoesNotThrow(() -> validator.validateReservationCreation(dto));
        }
    }

    @Test
    void validateMaxDurationAcceptsMaximumDuration() {
        LocalDateTime validStart = TEST_REFERENCE_DATE
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime validEnd = validStart.plusHours(24);

        assertDoesNotThrow(() -> validator.validateMaxDuration(validStart, validEnd));
    }

    @Test
    void validateCancellationAcceptsMinimumAdvanceNotice() {
        LocalDateTime mockedNow = LocalDateTime.of(2026, Month.JUNE, 10, 10, 0, 0);
        ZonedDateTime mockedZonedNow = mockedNow.atZone(ZoneId.systemDefault());

        LocalDateTime validStart = mockedNow
                .plusHours(24)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        try (MockedStatic<ZonedDateTime> mockedZDT = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            mockedZDT
                    .when(() -> ZonedDateTime.now(any(ZoneId.class)))
                    .thenReturn(mockedZonedNow);

            assertDoesNotThrow(() -> validator.validateCancellation(validStart));
        }
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