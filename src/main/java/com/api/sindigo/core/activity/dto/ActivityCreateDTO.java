package com.api.sindigo.core.activity.dto;

import lombok.Data;

@Data
public class ActivityCreateDTO {

    private String title;
    private String description;
    private Long buildingId;
}
