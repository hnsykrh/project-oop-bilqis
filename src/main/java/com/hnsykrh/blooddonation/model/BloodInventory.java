package com.hnsykrh.blooddonation.model;

import java.util.Objects;

public final class BloodInventory {

    private final int id;
    private final String bloodType;
    private final int stockMl;
    private final String updatedAt;

    public BloodInventory(int id, String bloodType, int stockMl, String updatedAt) {
        this.id = id;
        this.bloodType = Objects.requireNonNull(bloodType);
        this.stockMl = stockMl;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public int getId() {
        return id;
    }

    public String getBloodType() {
        return bloodType;
    }

    public int getStockMl() {
        return stockMl;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
