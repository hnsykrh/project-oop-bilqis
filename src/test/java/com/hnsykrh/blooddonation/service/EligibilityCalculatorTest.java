package com.hnsykrh.blooddonation.service;

import com.hnsykrh.blooddonation.model.Donor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityCalculatorTest {

    private static Donor donor(LocalDate dob, LocalDate lastDonation, boolean active) {
        return new Donor(1, "Test Donor", "012", "t@x.com", "O+", dob, lastDonation, active, "2026-01-01");
    }

    @ParameterizedTest(name = "Hb {0} g/dL -> eligible={1}")
    @CsvSource({
            "12.4, false",
            "12.5, true",
            "13.0, true",
            "20.0, true"
    })
    void hemoglobinThreshold(double hb, boolean expectedEligible) {
        Donor d = donor(LocalDate.of(2000, 1, 1), null, true);
        var result = EligibilityCalculator.evaluate(d, LocalDate.of(2026, 5, 19), hb);
        assertEquals(expectedEligible, result.eligible());
    }

    @ParameterizedTest(name = "days since last={0} -> eligible={1}")
    @CsvSource({
            "0, false",
            "89, false",
            "90, true",
            "365, true"
    })
    void donationInterval(int daysSince, boolean expectedEligible) {
        LocalDate last = LocalDate.of(2026, 1, 1);
        LocalDate proposed = last.plusDays(daysSince);
        Donor d = donor(LocalDate.of(1990, 1, 1), last, true);
        var result = EligibilityCalculator.evaluate(d, proposed, 14.0);
        assertEquals(expectedEligible, result.eligible());
    }

    @Test
    void inactiveDonorRejected() {
        Donor d = donor(LocalDate.of(1990, 1, 1), null, false);
        assertFalse(EligibilityCalculator.evaluate(d, LocalDate.now(), 14.0).eligible());
    }

    @Test
    void underageRejected() {
        Donor d = donor(LocalDate.of(2010, 1, 1), null, true);
        assertFalse(EligibilityCalculator.evaluate(d, LocalDate.of(2026, 1, 1), 14.0).eligible());
    }

    @Test
    void overageRejected() {
        Donor d = donor(LocalDate.of(1950, 1, 1), null, true);
        assertFalse(EligibilityCalculator.evaluate(d, LocalDate.of(2026, 1, 1), 14.0).eligible());
    }

    @Test
    void daysUntilNextEligibleWhenNoPriorDonation() {
        Donor d = donor(LocalDate.of(1990, 1, 1), null, true);
        assertEquals(0L, EligibilityCalculator.daysUntilNextEligible(d, LocalDate.now()));
    }
}
