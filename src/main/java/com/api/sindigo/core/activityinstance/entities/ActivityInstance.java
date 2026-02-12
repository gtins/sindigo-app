package com.api.sindigo.core.activityinstance.entities;

import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.activity.entities.ActivityStatus;
import com.api.sindigo.core.attachment.entities.Attachment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityInstance {

    @Id
    @GeneratedValue
    private UUID id;

    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    private ActivityStatus status;

    private UUID performedBy;
    private String notes;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @OneToOne
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;
}
