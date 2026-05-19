package com.hnsykrh.blooddonation.db;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/**
 * Model-layer persistence bootstrap: creates an on-disk SQLite database with five related tables.
 */
public final class DatabaseManager {

    private static final String JDBC_URL;

    static {
        Path dbPath = Path.of(System.getProperty("user.dir"), "blooddonation.db");
        JDBC_URL = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString().replace(File.separatorChar, '/');
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    public void initialize() {
        try (Connection connection = openConnection(); Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS staff (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password_plain TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        role TEXT NOT NULL,
                        is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
                        created_at TEXT NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS donors (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        full_name TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        email TEXT,
                        blood_type TEXT NOT NULL,
                        date_of_birth TEXT NOT NULL,
                        last_donation_date TEXT,
                        is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
                        created_at TEXT NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS donations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        donor_id INTEGER NOT NULL,
                        donation_date TEXT NOT NULL,
                        volume_ml INTEGER NOT NULL CHECK (volume_ml > 0),
                        hemoglobin_g_dl REAL NOT NULL,
                        staff_id INTEGER,
                        is_voided INTEGER NOT NULL DEFAULT 0 CHECK (is_voided IN (0, 1)),
                        void_reason TEXT,
                        created_at TEXT NOT NULL,
                        FOREIGN KEY (donor_id) REFERENCES donors(id),
                        FOREIGN KEY (staff_id) REFERENCES staff(id)
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS blood_inventory (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        blood_type TEXT NOT NULL UNIQUE,
                        stock_ml INTEGER NOT NULL DEFAULT 0 CHECK (stock_ml >= 0),
                        updated_at TEXT NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS recipient_requests (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        patient_reference TEXT NOT NULL,
                        hospital_name TEXT NOT NULL,
                        blood_type TEXT NOT NULL,
                        units_needed_ml INTEGER NOT NULL CHECK (units_needed_ml > 0),
                        request_date TEXT NOT NULL,
                        fulfilled_ml INTEGER NOT NULL DEFAULT 0 CHECK (fulfilled_ml >= 0),
                        is_cancelled INTEGER NOT NULL DEFAULT 0 CHECK (is_cancelled IN (0, 1)),
                        notes TEXT,
                        created_at TEXT NOT NULL
                    )""");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_donations_donor ON donations(donor_id)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_donations_date ON donations(donation_date)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_donors_blood ON donors(blood_type)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_requests_type ON recipient_requests(blood_type)");
            seedIfEmpty(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private void seedIfEmpty(Connection connection) throws SQLException {
        try (var ps = connection.prepareStatement("SELECT COUNT(*) FROM staff");
             var rs = ps.executeQuery()) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }
        String now = Instant.now().toString();
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    INSERT INTO staff (username, password_plain, full_name, role, is_active, created_at)
                    VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN', 1, '%s')""".formatted(now));
        }
        String[] types = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        try (var ps = connection.prepareStatement("""
                INSERT INTO blood_inventory (blood_type, stock_ml, updated_at) VALUES (?, ?, ?)""")) {
            for (String t : types) {
                ps.setString(1, t);
                ps.setInt(2, 0);
                ps.setString(3, now);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        seedDemoRecords(connection, now);
    }

    /**
     * Sample donors, one donation, and one hospital request so the GUI is usable immediately after first launch.
     */
    private void seedDemoRecords(Connection connection, String now) throws SQLException {
        try (var ps = connection.prepareStatement("""
                INSERT INTO donors (full_name, phone, email, blood_type, date_of_birth, last_donation_date, is_active, created_at)
                VALUES (?, ?, ?, ?, ?, ?, 1, ?)""")) {
            ps.setString(1, "Ahmad bin Hassan");
            ps.setString(2, "012-3456789");
            ps.setString(3, "ahmad@example.com");
            ps.setString(4, "O+");
            ps.setString(5, "1998-05-12");
            ps.setString(6, null);
            ps.setString(7, now);
            ps.executeUpdate();
            ps.setString(1, "Siti Nurhaliza");
            ps.setString(2, "019-8765432");
            ps.setString(3, "siti@example.com");
            ps.setString(4, "A+");
            ps.setString(5, "2000-08-20");
            ps.setString(6, "2025-11-01");
            ps.setString(7, now);
            ps.executeUpdate();
        }
        try (var ps = connection.prepareStatement("""
                INSERT INTO donations (donor_id, donation_date, volume_ml, hemoglobin_g_dl, staff_id, is_voided, created_at)
                VALUES (2, '2025-11-01', 450, 13.2, 1, 0, ?)""")) {
            ps.setString(1, now);
            ps.executeUpdate();
        }
        try (var ps = connection.prepareStatement(
                "UPDATE blood_inventory SET stock_ml = 450, updated_at = ? WHERE blood_type = 'A+'")) {
            ps.setString(1, now);
            ps.executeUpdate();
        }
        try (var ps = connection.prepareStatement("""
                INSERT INTO recipient_requests
                (patient_reference, hospital_name, blood_type, units_needed_ml, request_date, fulfilled_ml, is_cancelled, notes, created_at)
                VALUES ('PT-2026-001', 'City General Hospital', 'A+', 500, date('now'), 0, 0, 'Emergency surgery', ?)""")) {
            ps.setString(1, now);
            ps.executeUpdate();
        }
    }
}
