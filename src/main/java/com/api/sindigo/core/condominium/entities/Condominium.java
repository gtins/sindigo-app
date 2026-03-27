package com.api.sindigo.core.condominium.entities;

import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.membership.entities.Membership;
import com.api.sindigo.core.reservation.entities.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
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
public class Condominium {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String address;

    @CreationTimestamp
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL)
    private List<Activity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL)
    private List<Membership> members = new ArrayList<>();

    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL)
    private List<FinancialEntry> financialEntries = new ArrayList<>();
}

