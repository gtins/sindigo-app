package com.api.sindigo.core.membership;

import com.api.sindigo.core.membership.entities.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    
    List<Membership> findByCondominiumId(UUID condominiumId);
    
    Optional<Membership> findByCondominiumIdAndUserId(UUID condominiumId, UUID userId);
    
    List<Membership> findByUserId(UUID userId);
    
    void deleteByCondominiumIdAndUserId(UUID condominiumId, UUID userId);
    
    boolean existsByCondominiumIdAndUserId(UUID condominiumId, UUID userId);
}
