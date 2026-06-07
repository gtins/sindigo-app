package com.api.sindigo.core.reservation.validator;

import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class ReservationValidatorTest {

    private final ReservationValidator validator = new ReservationValidator();

    // Fixed test reference date (far future to ensure it's always valid)
    private static final LocalDateTime TEST_REFERENCE_DATE = LocalDateTime.of(2099, 12, 25, 12, 0, 0);

    @Test
    void validateReservationCreationAcceptsValidPayload() {
        // Fixed date: 2099-12-25 (far future, always valid for 7+ days from now)
        LocalDateTime validStart = TEST_REFERENCE_DATE.withHour(18).withMinute(0).withSecond(0).withNano(0);
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
        // Time range test doesn't depend on system clock, using fixed date
        LocalDateTime baseDate = TEST_REFERENCE_DATE.withHour(18).withMinute(0).withSecond(0).withNano(0);
        
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
        // Mock LocalDateTime.now() to return a fixed reference time
        LocalDateTime mockedNow = LocalDateTime.of(2026, 6, 10, 10, 0, 0);
        LocalDateTime invalidStart = mockedNow.plusDays(3).withHour(18).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime invalidEnd = invalidStart.plusHours(4);
        
        try (MockedStatic<LocalDateTime> mockedDateTime = mockStatic(LocalDateTime.class)) {
            mockedDateTime.when(LocalDateTime::now).thenReturn(mockedNow);
            
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
    }

    @Test
    void validateReservationCreationRejectsExcessiveDuration() {
        // Fixed date: 2099-12-25 (far future, always valid for 7+ days from now)
        // But exceeds the 6-hour maximum duration
        LocalDateTime validStart = TEST_REFERENCE_DATE.withHour(10).withMinute(0).withSecond(0).withNano(0);
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

