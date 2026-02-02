package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByBuildingId(Long buildingId);
}
