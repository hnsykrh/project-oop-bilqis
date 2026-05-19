package com.hnsykrh.blooddonation;

import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.support.TestDatabase;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Static checks against BAXU 3113 PDF Table 1 (database tables, MVC packages).
 */
class RubricComplianceTest {

    @Test
    void databaseHasFiveInterconnectedTables() throws Exception {
        DatabaseManager db = TestDatabase.createEmpty();
        Set<String> tables = new HashSet<>();
        try (Connection c = db.openConnection();
             ResultSet rs = c.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
        }
        assertTrue(tables.contains("staff"));
        assertTrue(tables.contains("donors"));
        assertTrue(tables.contains("donations"));
        assertTrue(tables.contains("blood_inventory"));
        assertTrue(tables.contains("recipient_requests"));
        assertEquals(5, tables.size());
    }

    @Test
    void mvcLayersPresent() throws ClassNotFoundException {
        assertNotMissing("com.hnsykrh.blooddonation.model.Donor");
        assertNotMissing("com.hnsykrh.blooddonation.view.MainFrame");
        assertNotMissing("com.hnsykrh.blooddonation.controller.DonorController");
        assertNotMissing("com.hnsykrh.blooddonation.dao.DonorDao");
        assertNotMissing("com.hnsykrh.blooddonation.service.EligibilityCalculator");
    }

    private static void assertNotMissing(String className) throws ClassNotFoundException {
        Class.forName(className);
    }
}
