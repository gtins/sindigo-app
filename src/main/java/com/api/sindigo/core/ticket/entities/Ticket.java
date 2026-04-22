package com.api.sindigo.core.ticket.entities;

import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.user.entities.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String description;
    private String location;              // "Ap 102", "Área comum", etc

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime closedAt;       // Quando foi fechado

    // FK: O condomínio ao qual pertence
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", nullable = false)
    private Condominium condominium;

    // FK: Quem abriu o chamado (morador, síndico, etc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // FK: Quem atendeu/responsável (síndico, gerente, etc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = true)
    private User assignedTo;

    // Relacionamento: Um ticket pode gerar múltiplas atividades
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.PERSIST)
    private List<Activity> activities = new ArrayList<>();

    // Auditoria adicional opcional
    private String notes;                  // Notas internas do síndico
    private String estimatedResolution;    // Data/período estimado
}

