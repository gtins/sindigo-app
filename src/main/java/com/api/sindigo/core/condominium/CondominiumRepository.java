package com.api.sindigo.core.condominium;

import com.api.sindigo.core.condominium.entities.Condominium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CondominiumRepository extends JpaRepository<Condominium, UUID> {
    List<Condominium> findByOwnerId(UUID ownerId);

    Optional<Condominium> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("SELECT c FROM Condominium c " +
           "LEFT JOIN Membership m ON c.id = m.condominium.id " +
           "WHERE c.id = :condominiumId AND (c.owner.id = :userId OR m.user.id = :userId)")
    Optional<Condominium> findByIdAndUserHasAccess(@Param("condominiumId") UUID condominiumId, @Param("userId") UUID userId);
}

