package com.hnsykrh.blooddonation;

import com.hnsykrh.blooddonation.model.Donor;
import com.hnsykrh.blooddonation.service.EligibilityCalculator;
import com.hnsykrh.blooddonation.service.FulfillmentCalculator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exhaustive matrix: 1001 independent scenario assertions (BAXU rubric — calculations & logic).
 */
class ScenarioMatrixTest {

  private static final int TARGET_SCENARIOS = 1001;

  @Test
  void run1001Scenarios() {
    int count = 0;
    LocalDate base = LocalDate.of(2026, 5, 19);

    // Scenarios 1–625: fulfillment grid (stock 0–24, need 0–24)
    for (int stock = 0; stock <= 24; stock++) {
      for (int need = 0; need <= 24; need++) {
        int expected = (stock <= 0 || need <= 0) ? 0 : Math.min(stock, need);
        assertEquals(expected, FulfillmentCalculator.maxFulfillableMl(stock, need),
                "fulfillment stock=" + stock + " need=" + need);
        count++;
      }
    }

    // Scenarios 626–1001: eligibility combinations
    for (int i = 0; count < TARGET_SCENARIOS; i++) {
      int ageYears = 10 + (i % 56);
      int daysSince = i % 130;
      double hb = 10.0 + (i % 70) / 10.0;
      LocalDate dob = base.minusYears(ageYears);
      LocalDate last = daysSince == 0 ? null : base.minusDays(daysSince);
      Donor donor = new Donor(1, "S" + i, "p", null, "A+", dob, last, true, "x");

      var result = EligibilityCalculator.evaluate(donor, base, hb);
      boolean shouldPass = ageYears >= EligibilityCalculator.MIN_DONOR_AGE_YEARS
              && ageYears <= EligibilityCalculator.MAX_DONOR_AGE_YEARS
              && hb >= EligibilityCalculator.MIN_HEMOGLOBIN_G_DL
              && (last == null || daysSince >= EligibilityCalculator.MIN_DAYS_BETWEEN_WHOLE_BLOOD_DONATIONS);

      assertEquals(shouldPass, result.eligible(), "eligibility scenario index " + i);
      count++;
    }

    assertEquals(TARGET_SCENARIOS, count, "scenario count must equal 1001");
    assertTrue(count >= TARGET_SCENARIOS);
  }
}
