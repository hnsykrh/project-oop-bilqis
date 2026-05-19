package com.hnsykrh.blooddonation.dao;

import com.hnsykrh.blooddonation.model.Donation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DonationDao {

    public Optional<Donation> findById(Connection connection, int id) throws SQLException {
        String sql = """
                SELECT d.id, d.donor_id, dn.full_name AS donor_name, dn.blood_type AS donor_blood_type,
                       d.donation_date, d.volume_ml, d.hemoglobin_g_dl, d.staff_id,
                       d.is_voided, d.void_reason, d.created_at
                FROM donations d
                JOIN donors dn ON dn.id = d.donor_id
                WHERE d.id = ?""";
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

    public List<Donation> search(Connection connection, String query, boolean nonVoidedOnly) throws SQLException {
        String sql = """
                SELECT d.id, d.donor_id, dn.full_name AS donor_name, dn.blood_type AS donor_blood_type,
                       d.donation_date, d.volume_ml, d.hemoglobin_g_dl, d.staff_id,
                       d.is_voided, d.void_reason, d.created_at
                FROM donations d
                JOIN donors dn ON dn.id = d.donor_id
                WHERE (? = 0 OR d.is_voided = 0)
                  AND (? IS NULL OR ? = ''
                       OR dn.full_name LIKE '%' || ? || '%'
                       OR dn.blood_type LIKE '%' || ? || '%'
                       OR CAST(d.id AS TEXT) LIKE '%' || ? || '%')
                ORDER BY d.donation_date DESC, d.id DESC""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, nonVoidedOnly ? 1 : 0);
            boolean hasQ = query != null && !query.isBlank();
            String q = hasQ ? query.trim() : "";
            ps.setString(2, hasQ ? q : null);
            ps.setString(3, hasQ ? q : null);
            ps.setString(4, hasQ ? q : null);
            ps.setString(5, hasQ ? q : null);
            ps.setString(6, hasQ ? q : null);
            ps.setString(7, hasQ ? q : null);
            try (ResultSet rs = ps.executeQuery()) {
                List<Donation> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }
        }
    }

    public int insert(Connection connection, int donorId, LocalDate donationDate, int volumeMl,
                      double hemoglobinGdl, Integer staffId) throws SQLException {
        String sql = """
                INSERT INTO donations (donor_id, donation_date, volume_ml, hemoglobin_g_dl, staff_id,
                                       is_voided, void_reason, created_at)
                VALUES (?, ?, ?, ?, ?, 0, NULL, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, donorId);
            ps.setString(2, donationDate.toString());
            ps.setInt(3, volumeMl);
            ps.setDouble(4, hemoglobinGdl);
            if (staffId == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, staffId);
            }
            ps.setString(6, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void voidDonation(Connection connection, int donationId, String voidReason) throws SQLException {
        String sql = """
                UPDATE donations
                SET is_voided = 1, void_reason = ?
                WHERE id = ? AND is_voided = 0""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, voidReason);
            ps.setInt(2, donationId);
            ps.executeUpdate();
        }
    }

    /**
     * Volume of non-voided donations grouped by donor blood type (for analytics chart).
     */
    public Map<String, Integer> volumeByBloodType(Connection connection) throws SQLException {
        String sql = """
                SELECT dn.blood_type, COALESCE(SUM(d.volume_ml), 0) AS total_ml
                FROM donations d
                JOIN donors dn ON dn.id = d.donor_id
                WHERE d.is_voided = 0
                GROUP BY dn.blood_type
                ORDER BY dn.blood_type""";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Map<String, Integer> out = new HashMap<>();
            while (rs.next()) {
                out.put(rs.getString("blood_type"), rs.getInt("total_ml"));
            }
            return out;
        }
    }

    private static Donation mapRow(ResultSet rs) throws SQLException {
        int staffId = rs.getInt("staff_id");
        Integer staff = rs.wasNull() ? null : staffId;
        return new Donation(
                rs.getInt("id"),
                rs.getInt("donor_id"),
                rs.getString("donor_name"),
                rs.getString("donor_blood_type"),
                LocalDate.parse(rs.getString("donation_date")),
                rs.getInt("volume_ml"),
                rs.getDouble("hemoglobin_g_dl"),
                staff,
                rs.getInt("is_voided") == 1,
                rs.getString("void_reason"),
                rs.getString("created_at")
        );
    }
}
