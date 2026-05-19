package com.hnsykrh.blooddonation.service;

/**
 * Model domain logic for matching hospital requests to physical stock: fulfillment cannot exceed
 * remaining clinical need or available inventory (whichever is smaller).
 */
public final class FulfillmentCalculator {

    private FulfillmentCalculator() {
    }

    /**
     * @param stockMl        current stock for the requested blood type
     * @param remainingNeedMl {@code units_needed_ml - fulfilled_ml} (non-negative)
     * @return milliliters that can be allocated in one step
     */
    public static int maxFulfillableMl(int stockMl, int remainingNeedMl) {
        if (stockMl <= 0 || remainingNeedMl <= 0) {
            return 0;
        }
        return Math.min(stockMl, remainingNeedMl);
    }
}
