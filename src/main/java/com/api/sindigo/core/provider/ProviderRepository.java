package com.api.sindigo.core.provider;

import com.api.sindigo.core.provider.entities.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, UUID> {

    List<Provider> findByCondominiumIdOrderByNameAsc(UUID condominiumId);

    Optional<Provider> findByIdAndCondominiumId(UUID id, UUID condominiumId);

    @Query("SELECT p FROM Provider p WHERE p.condominium.id = :condominiumId AND LOWER(p.serviceType) = LOWER(:serviceType)")
    List<Provider> findByCondominiumAndServiceType(@Param("condominiumId") UUID condominiumId, @Param("serviceType") String serviceType);
}

