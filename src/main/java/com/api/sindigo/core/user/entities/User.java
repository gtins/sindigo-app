package com.api.sindigo.core.user.entities;

import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.membership.entities.Membership;
import com.api.sindigo.core.reservation.entities.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.MORADOR;

    @CreationTimestamp
    private LocalDate createdAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Condominium> ownedCondominiums = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Membership> condominiums = new ArrayList<>();

    @OneToMany(mappedBy = "requestedBy")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "uploadedBy")
    private List<Attachment> uploads = new ArrayList<>();
}

