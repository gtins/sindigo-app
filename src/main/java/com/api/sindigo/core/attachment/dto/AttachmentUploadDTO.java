package com.api.sindigo.core.attachment.dto;

import com.api.sindigo.core.attachment.enums.AttachmentCategory;
import com.api.sindigo.core.activityinstance.entities.ActivityInstance;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.user.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentUploadDTO {
    private Condominium condominium;
    private Ticket ticket;
    private ActivityInstance activity;
    private Provider serviceProvider;
    private User uploadedBy;
    private AttachmentCategory category;
}

