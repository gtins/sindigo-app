package com.api.sindigo.core.activityinstance;

import com.api.sindigo.core.activityinstance.entities.ActivityInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityInstanceRepository extends JpaRepository<ActivityInstance, UUID> {

    List<ActivityInstance> findByActivityId(UUID activityId);

}

