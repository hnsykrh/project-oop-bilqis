package com.hnsykrh.blooddonation.model;

import java.time.LocalDate;
import java.util.Objects;

public final class RecipientRequest {

    private final int id;
    private final String patientReference;
    private final String hospitalName;
    private final String bloodType;
    private final int unitsNeededMl;
    private final LocalDate requestDate;
    private final int fulfilledMl;
    private final boolean cancelled;
    private final String notes;
    private final String createdAt;

    public RecipientRequest(int id, String patientReference, String hospitalName, String bloodType,
                            int unitsNeededMl, LocalDate requestDate, int fulfilledMl, boolean cancelled,
                            String notes, String createdAt) {
        this.id = id;
        this.patientReference = Objects.requireNonNull(patientReference);
        this.hospitalName = Objects.requireNonNull(hospitalName);
        this.bloodType = Objects.requireNonNull(bloodType);
        this.unitsNeededMl = unitsNeededMl;
        this.requestDate = Objects.requireNonNull(requestDate);
        this.fulfilledMl = fulfilledMl;
        this.cancelled = cancelled;
        this.notes = notes;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public int getId() {
        return id;
    }

    public String getPatientReference() {
        return patientReference;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getBloodType() {
        return bloodType;
    }

    public int getUnitsNeededMl() {
        return unitsNeededMl;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public int getFulfilledMl() {
        return fulfilledMl;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int remainingMl() {
        return Math.max(0, unitsNeededMl - fulfilledMl);
    }
}
