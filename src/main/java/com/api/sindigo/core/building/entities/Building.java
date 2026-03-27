package com.api.sindigo.core.building.entities;

// DEPRECATED: Use com.api.sindigo.core.condominium.entities.Condominium instead
// This class is kept for reference only and should be deleted

/*
import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.membership.entities.Membership;
import com.api.sindigo.core.reservation.entities.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "condominiums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String address;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // 1 -> N Activities
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
    private List<Activity> activities = new ArrayList<>();

    // 1 -> N CondoUser
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL)
    private List<Membership> members = new ArrayList<>();

    // 1 -> N Reservations
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    // 1 -> N Financial Entries
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL)
    private List<FinancialEntry> financialEntries = new ArrayList<>();
}
*/

