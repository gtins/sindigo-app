package com.api.sindigo.core.audit.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String action;
    private String resource;
    private String url;
    private String changes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDate createdDate;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

}
