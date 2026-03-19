package com.api.sindigo.core.reservation.entities;

import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.user.entities.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
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
    private LocalDate startTime;
    private LocalDate endTime;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @CreationTimestamp
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;

    @ManyToOne
    @JoinColumn(name = "condominium_id")
    private Condominium condominium;

    @ManyToOne
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @OneToOne
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;
}
