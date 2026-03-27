package com.api.sindigo.core.reservation.validator;

import com.api.sindigo.core.reservation.dto.ReservationCreateDTO;
import com.api.sindigo.validator.BaseValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationValidator extends BaseValidator {

    public void validateReservationCreation(ReservationCreateDTO dto) {
        validateStringNotEmpty(dto.getArea(), "Área");
        validateTimeRange(dto.getStartTime(), dto.getEndTime());
    }

    public void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        validateCondition(
            startTime.isBefore(endTime),
            "Hora de início deve ser antes da hora de fim"
        );
    }

    public void validateNoConflicts(boolean hasConflicts) {
        validateCondition(
            !hasConflicts,
            "Já existe uma reserva para esta área no período solicitado"
        );
    }
}

