package com.api.sindigo.core.membership.entities;

import com.api.sindigo.core.building.entities.Building;
import com.api.sindigo.core.user.entities.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import javax.management.relation.Role;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "condo_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "condominium_id")
    private Building building;
}
