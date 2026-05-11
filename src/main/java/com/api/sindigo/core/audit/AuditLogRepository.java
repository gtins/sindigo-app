package com.api.sindigo.core.audit;

import com.api.sindigo.core.audit.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByCreatedBy(String createdBy, Pageable pageable);

    Page<AuditLog> findByResource(String resource, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    List<AuditLog> findByResourceId(UUID resourceId);

    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
