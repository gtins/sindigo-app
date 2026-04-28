package com.api.sindigo.core.user.entities;

public enum UserRole {
    ADMIN("Administrador"),
    SINDICO("Síndico"),
    MORADOR("Morador");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

