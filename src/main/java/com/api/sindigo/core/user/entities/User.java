package com.api.sindigo.core.user.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "sindigo_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="username", unique = true)
    private String username;
    @Column(name="password")
    private String password;
}
