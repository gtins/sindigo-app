package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findByCondominiumId(UUID condominiumId);
}
