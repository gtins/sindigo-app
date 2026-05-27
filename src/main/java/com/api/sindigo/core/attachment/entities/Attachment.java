package com.api.sindigo.core.attachment.entities;

import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.activityinstance.entities.ActivityInstance;
import com.api.sindigo.core.provider.entities.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachments_condominium_id", columnList = "condominium_id"),
        @Index(name = "idx_attachments_ticket_id", columnList = "ticket_id"),
        @Index(name = "idx_attachments_activity_id", columnList = "activity_id"),
        @Index(name = "idx_attachments_provider_id", columnList = "service_provider_id"),
        @Index(name = "idx_attachments_category", columnList = "attachment_category"),
        @Index(name = "idx_attachments_storage_key", columnList = "storage_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", nullable = false)
    private Condominium condominium;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private ActivityInstance activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id")
    private Provider serviceProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "attachment_category", nullable = false)
    private String attachmentCategory; // TICKET_EVIDENCE, INVOICE, COMPLETION_PROOF, etc.

    @Column(name = "file_type", nullable = false)
    private String fileType; // PDF, PNG, JPG, etc.

    @Column(name = "original_file_name", nullable = false, length = 500)
    private String originalFileName;

    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey; // S3 object key: YYYY/MM/UUID-filename

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType; // application/pdf, image/png, etc.

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
