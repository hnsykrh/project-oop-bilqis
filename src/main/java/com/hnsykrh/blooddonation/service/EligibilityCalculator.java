package com.hnsykrh.blooddonation.service;

import com.hnsykrh.blooddonation.model.Donor;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Domain calculation used before accepting a donation: spacing between donations and hemoglobin threshold.
 */
public final class EligibilityCalculator {

    public static final int MIN_DAYS_BETWEEN_WHOLE_BLOOD_DONATIONS = 90;
    public static final int MIN_DONOR_AGE_YEARS = 18;
    public static final int MAX_DONOR_AGE_YEARS = 65;
    public static final double MIN_HEMOGLOBIN_G_DL = 12.5;

    private EligibilityCalculator() {
    }

    public static EligibilityResult evaluate(Donor donor, LocalDate proposedDonationDate, double hemoglobinGdl) {
        Objects.requireNonNull(donor);
        Objects.requireNonNull(proposedDonationDate);
        if (!donor.isActive()) {
            return new EligibilityResult(false, "Donor record is inactive (soft-deleted).", null);
        }
        int age = Period.between(donor.getDateOfBirth(), proposedDonationDate).getYears();
        if (age < MIN_DONOR_AGE_YEARS) {
            return new EligibilityResult(false, "Donor is below minimum age (" + MIN_DONOR_AGE_YEARS + ").", null);
        }
        if (age > MAX_DONOR_AGE_YEARS) {
            return new EligibilityResult(false, "Donor exceeds maximum age (" + MAX_DONOR_AGE_YEARS + ") for this module.", null);
        }
        if (hemoglobinGdl < MIN_HEMOGLOBIN_G_DL) {
            return new EligibilityResult(false,
                    "Hemoglobin too low: " + String.format("%.1f", hemoglobinGdl) + " g/dL (min "
                            + MIN_HEMOGLOBIN_G_DL + ").",
                    null);
        }
        if (donor.getLastDonationDate() != null) {
            long daysSince = ChronoUnit.DAYS.between(donor.getLastDonationDate(), proposedDonationDate);
            if (daysSince < MIN_DAYS_BETWEEN_WHOLE_BLOOD_DONATIONS) {
                long waitMore = MIN_DAYS_BETWEEN_WHOLE_BLOOD_DONATIONS - daysSince;
                return new EligibilityResult(false,
                        "Minimum interval not met: " + daysSince + " days since last donation (requires "
                                + MIN_DAYS_BETWEEN_WHOLE_BLOOD_DONATIONS + ").",
                        waitMore);
            }
        }
        return new EligibilityResult(true, "Eligible to donate.", 0L);
    }

    /**
     * Whole-blood spacing rule: days from {@code asOf} until the donor may donate again under the
     * 90-day minimum interval. Returns {@code 0} when already eligible by date (or no prior donation).
     */
    public static long daysUntilNextEligible(Donor donor, LocalDate asOf) {
        Objects.requireNonNull(donor);
        Objects.requireNonNull(asOf);
        if (donor.getLastDonationDate() == null) {
            return 0L;
        }
        long daysSince = ChronoUnit.DAYS.between(donor.getLastDonationDate(), asOf);
        if (daysSince >= MIN_DAYS_BETWEEN_WHOLE_BLOOD_DONATIONS) {
            return 0L;
        }
        return MIN_DAYS_BETWEEN_WHOLE_BLOOD_DONATIONS - daysSince;
    }

    public record EligibilityResult(boolean eligible, String message, Long daysRemainingIfBlocked) {
    }
}
