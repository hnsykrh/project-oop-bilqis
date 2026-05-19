package com.hnsykrh.blooddonation.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FulfillmentCalculatorTest {

    @ParameterizedTest(name = "stock={0}, need={1} -> max={2}")
    @CsvSource({
            "0, 500, 0",
            "100, 0, 0",
            "100, 50, 50",
            "100, 200, 100",
            "1000, 250, 250"
    })
    void maxFulfillable(int stock, int need, int expected) {
        assertEquals(expected, FulfillmentCalculator.maxFulfillableMl(stock, need));
    }
}
