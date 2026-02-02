package com.api.sindigo.core.building.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "sindigo_buildings")
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "name", unique = false)
    private String name;
    @Column(name="adress", unique = false)
    private String adress;
    @Column(name="active", unique = false)
    private Boolean active;
    @Column(name="date", unique = false)
    private String date;

}
