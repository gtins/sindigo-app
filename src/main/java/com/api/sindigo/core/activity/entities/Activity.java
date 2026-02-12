package com.api.sindigo.core.activity.entities;

import com.api.sindigo.core.activityinstance.entities.ActivityInstance;
import com.api.sindigo.core.building.entities.Building;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ActivityType type;

    private LocalDate startDate;
    private LocalDate endDate;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<ActivityInstance> instances = new ArrayList<>();
}
