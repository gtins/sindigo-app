package com.api.sindigo.core.reservation.validator;

import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.validator.BaseValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class ReservationValidator extends BaseValidator {

    private static final int MINIMUM_ADVANCE_DAYS = 7;
    private static final int MAXIMUM_DURATION_HOURS = 6;

    public void validateReservationCreation(ReservationCreateDTO dto) {
        validateStringNotEmpty(dto.getArea(), "Área");
        validateStringNotEmpty(dto.getUnitNumber(), "Unidade");
        validateTimeRange(dto.getStartTime(), dto.getEndTime());
        validateAdvanceNotice(dto.getStartTime());
        validateMaxDuration(dto.getStartTime(), dto.getEndTime());
    }

    public void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        validateCondition(
            startTime.isBefore(endTime),
            "Hora de início deve ser antes da hora de fim"
        );
    }

    public void validateAdvanceNotice(LocalDateTime startTime) {
        LocalDateTime now = LocalDateTime.now();
        long daysUntilReservation = ChronoUnit.DAYS.between(now, startTime);
        
        validateCondition(
            daysUntilReservation >= MINIMUM_ADVANCE_DAYS,
            "As reservas devem ser feitas com no mínimo " + MINIMUM_ADVANCE_DAYS + " dias de antecedência"
        );
    }

    public void validateMaxDuration(LocalDateTime startTime, LocalDateTime endTime) {
        long durationHours = ChronoUnit.HOURS.between(startTime, endTime);
        
        validateCondition(
            durationHours <= MAXIMUM_DURATION_HOURS,
            "O período máximo permitido para uma reserva é de " + MAXIMUM_DURATION_HOURS + " horas"
        );
    }

    public void validateNoConflicts(boolean hasConflicts) {
        validateCondition(
            !hasConflicts,
            "Já existe uma reserva para esta área no período solicitado"
        );
    }
}

