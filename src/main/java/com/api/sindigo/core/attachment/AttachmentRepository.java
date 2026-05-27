package com.api.sindigo.core.attachment;

import com.api.sindigo.core.attachment.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByTicketIdAndDeletedAtNull(UUID ticketId);

    List<Attachment> findByActivityIdAndDeletedAtNull(UUID activityId);

    List<Attachment> findByServiceProviderIdAndDeletedAtNull(UUID serviceProviderId);

    List<Attachment> findByCondominiumIdAndDeletedAtNull(UUID condominiumId);

    Optional<Attachment> findByStorageKeyAndDeletedAtNull(String storageKey);

    @Query("SELECT a FROM Attachment a WHERE a.ticket.id = :ticketId AND a.attachmentCategory = :category AND a.deletedAt IS NULL")
    List<Attachment> findByTicketAndCategory(@Param("ticketId") UUID ticketId, @Param("category") String category);

    @Query("SELECT a FROM Attachment a WHERE a.activity.id = :activityId AND a.attachmentCategory = :category AND a.deletedAt IS NULL")
    List<Attachment> findByActivityAndCategory(@Param("activityId") UUID activityId, @Param("category") String category);
}
