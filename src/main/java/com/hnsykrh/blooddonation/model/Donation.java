package com.hnsykrh.blooddonation.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Donation event (soft delete / void via {@code voided}).
 */
public final class Donation {

    private final int id;
    private final int donorId;
    private final String donorName;
    private final String donorBloodType;
    private final LocalDate donationDate;
    private final int volumeMl;
    private final double hemoglobinGdl;
    private final Integer staffId;
    private final boolean voided;
    private final String voidReason;
    private final String createdAt;

    public Donation(int id, int donorId, String donorName, String donorBloodType, LocalDate donationDate,
                    int volumeMl, double hemoglobinGdl, Integer staffId, boolean voided,
                    String voidReason, String createdAt) {
        this.id = id;
        this.donorId = donorId;
        this.donorName = Objects.requireNonNull(donorName);
        this.donorBloodType = Objects.requireNonNull(donorBloodType);
        this.donationDate = Objects.requireNonNull(donationDate);
        this.volumeMl = volumeMl;
        this.hemoglobinGdl = hemoglobinGdl;
        this.staffId = staffId;
        this.voided = voided;
        this.voidReason = voidReason;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public int getId() {
        return id;
    }

    public int getDonorId() {
        return donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDonorBloodType() {
        return donorBloodType;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public double getHemoglobinGdl() {
        return hemoglobinGdl;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public boolean isVoided() {
        return voided;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
