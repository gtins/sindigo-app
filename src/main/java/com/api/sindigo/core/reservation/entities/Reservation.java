package com.api.sindigo.core.reservation.entities;

import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.core.building.entities.Building;
import com.api.sindigo.core.user.entities.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue
    private UUID id;

    private String area;
    private Instant startTime;
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @ManyToOne
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @OneToOne
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;
}
