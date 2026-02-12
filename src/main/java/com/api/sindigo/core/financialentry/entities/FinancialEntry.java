package com.api.sindigo.core.financialentry.entities;

import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.core.building.entities.Building;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "financial_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private FinancialType type;

    private BigDecimal amount;
    private LocalDate date;
    private String description;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @OneToOne
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;
}
