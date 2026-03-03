package com.api.sindigo.core.reservation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCreateDTO {

    @NotBlank(message = "Area is required")
    private String area;

    @NotNull(message = "Start time is required")
    @JsonProperty("start_time")
    private Instant startTime;

    @NotNull(message = "End time is required")
    @JsonProperty("end_time")
    private Instant endTime;
}

