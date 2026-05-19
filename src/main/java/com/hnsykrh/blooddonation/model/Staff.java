package com.hnsykrh.blooddonation.model;

import java.util.Objects;

public final class Staff {

    private final int id;
    private final String username;
    private final String passwordPlain;
    private final String fullName;
    private final String role;
    private final boolean active;
    private final String createdAt;

    public Staff(int id, String username, String passwordPlain, String fullName, String role,
                 boolean active, String createdAt) {
        this.id = id;
        this.username = Objects.requireNonNull(username);
        this.passwordPlain = Objects.requireNonNull(passwordPlain);
        this.fullName = Objects.requireNonNull(fullName);
        this.role = Objects.requireNonNull(role);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordPlain() {
        return passwordPlain;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
