package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findByCondominiumId(UUID condominiumId);

    List<Activity> findByCondominiumIdAndCreatedById(UUID condominiumId, UUID createdById);

    Optional<Activity> findByIdAndCreatedById(UUID id, UUID createdById);

    Optional<Activity> findByIdAndCondominiumId(UUID id, UUID condominiumId);
}
