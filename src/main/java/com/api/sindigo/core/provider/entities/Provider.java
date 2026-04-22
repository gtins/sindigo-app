package com.api.sindigo.core.provider.entities;

import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.condominium.entities.Condominium;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String serviceType;           // "Encanador", "Eletricista", etc
    private String phone;
    private String email;
    private String notes;

    @CreationTimestamp
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;

    // FK: A qual condomínio está vinculado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", nullable = false)
    private Condominium condominium;

    // Relacionamento: Um prestador pode ter executado múltiplas atividades
    @OneToMany(mappedBy = "provider", cascade = CascadeType.PERSIST)
    private List<Activity> activities = new ArrayList<>();
}

