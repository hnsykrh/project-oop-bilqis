package com.hnsykrh.blooddonation.dao;

import com.hnsykrh.blooddonation.model.RecipientRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RecipientRequestDao {

    public Optional<RecipientRequest> findById(Connection connection, int id) throws SQLException {
        String sql = """
                SELECT id, patient_reference, hospital_name, blood_type, units_needed_ml, request_date,
                       fulfilled_ml, is_cancelled, notes, created_at
                FROM recipient_requests WHERE id = ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    public List<RecipientRequest> search(Connection connection, String query, boolean openOnly) throws SQLException {
        String sql = """
                SELECT id, patient_reference, hospital_name, blood_type, units_needed_ml, request_date,
                       fulfilled_ml, is_cancelled, notes, created_at
                FROM recipient_requests
                WHERE (? = 0 OR (is_cancelled = 0 AND fulfilled_ml < units_needed_ml))
                  AND (? IS NULL OR ? = ''
                       OR patient_reference LIKE '%' || ? || '%'
                       OR hospital_name LIKE '%' || ? || '%'
                       OR blood_type LIKE '%' || ? || '%')
                ORDER BY request_date DESC, id DESC""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, openOnly ? 1 : 0);
            boolean hasQ = query != null && !query.isBlank();
            String q = hasQ ? query.trim() : "";
            ps.setString(2, hasQ ? q : null);
            ps.setString(3, hasQ ? q : null);
            ps.setString(4, hasQ ? q : null);
            ps.setString(5, hasQ ? q : null);
            ps.setString(6, hasQ ? q : null);
            ps.setString(7, hasQ ? q : null);
            try (ResultSet rs = ps.executeQuery()) {
                List<RecipientRequest> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }
        }
    }

    public int insert(Connection connection, String patientReference, String hospitalName, String bloodType,
                      int unitsNeededMl, LocalDate requestDate, String notes) throws SQLException {
        String sql = """
                INSERT INTO recipient_requests (patient_reference, hospital_name, blood_type, units_needed_ml,
                                                request_date, fulfilled_ml, is_cancelled, notes, created_at)
                VALUES (?, ?, ?, ?, ?, 0, 0, ?, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patientReference);
            ps.setString(2, hospitalName);
            ps.setString(3, bloodType);
            ps.setInt(4, unitsNeededMl);
            ps.setString(5, requestDate.toString());
            ps.setString(6, notes);
            ps.setString(7, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void update(Connection connection, int id, String patientReference, String hospitalName, String bloodType,
                       int unitsNeededMl, LocalDate requestDate, String notes) throws SQLException {
        String sql = """
                UPDATE recipient_requests
                SET patient_reference = ?, hospital_name = ?, blood_type = ?, units_needed_ml = ?,
                    request_date = ?, notes = ?
                WHERE id = ? AND is_cancelled = 0""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, patientReference);
            ps.setString(2, hospitalName);
            ps.setString(3, bloodType);
            ps.setInt(4, unitsNeededMl);
            ps.setString(5, requestDate.toString());
            ps.setString(6, notes);
            ps.setInt(7, id);
            ps.executeUpdate();
        }
    }

    public void addFulfilledMl(Connection connection, int id, int deltaMl) throws SQLException {
        String sql = """
                UPDATE recipient_requests
                SET fulfilled_ml = fulfilled_ml + ?
                WHERE id = ? AND is_cancelled = 0
                  AND fulfilled_ml + ? <= units_needed_ml""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, deltaMl);
            ps.setInt(2, id);
            ps.setInt(3, deltaMl);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Cannot update fulfilled amount for request " + id);
            }
        }
    }

    public void setCancelled(Connection connection, int id, boolean cancelled) throws SQLException {
        String sql = "UPDATE recipient_requests SET is_cancelled = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cancelled ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private static RecipientRequest mapRow(ResultSet rs) throws SQLException {
        return new RecipientRequest(
                rs.getInt("id"),
                rs.getString("patient_reference"),
                rs.getString("hospital_name"),
                rs.getString("blood_type"),
                rs.getInt("units_needed_ml"),
                LocalDate.parse(rs.getString("request_date")),
                rs.getInt("fulfilled_ml"),
                rs.getInt("is_cancelled") == 1,
                rs.getString("notes"),
                rs.getString("created_at")
        );
    }
}
