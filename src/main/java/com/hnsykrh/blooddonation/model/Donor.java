package com.hnsykrh.blooddonation.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Donor aggregate stored in {@code donors} table (supports soft delete via {@code active} flag).
 */
public final class Donor {

    private final int id;
    private final String fullName;
    private final String phone;
    private final String email;
    private final String bloodType;
    private final LocalDate dateOfBirth;
    private final LocalDate lastDonationDate;
    private final boolean active;
    private final String createdAt;

    public Donor(int id, String fullName, String phone, String email, String bloodType,
                 LocalDate dateOfBirth, LocalDate lastDonationDate, boolean active, String createdAt) {
        this.id = id;
        this.fullName = Objects.requireNonNull(fullName);
        this.phone = Objects.requireNonNull(phone);
        this.email = email;
        this.bloodType = Objects.requireNonNull(bloodType);
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
        this.lastDonationDate = lastDonationDate;
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getBloodType() {
        return bloodType;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
