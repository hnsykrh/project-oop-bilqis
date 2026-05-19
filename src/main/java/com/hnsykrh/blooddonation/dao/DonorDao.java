package com.hnsykrh.blooddonation.dao;

import com.hnsykrh.blooddonation.model.Donor;

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

public final class DonorDao {

    public Optional<Donor> findById(Connection connection, int id) throws SQLException {
        String sql = """
                SELECT id, full_name, phone, email, blood_type, date_of_birth, last_donation_date,
                       is_active, created_at
                FROM donors WHERE id = ?""";
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

    public List<Donor> search(Connection connection, String query, boolean activeOnly) throws SQLException {
        String sql = """
                SELECT id, full_name, phone, email, blood_type, date_of_birth, last_donation_date,
                       is_active, created_at
                FROM donors
                WHERE (? = 0 OR is_active = 1)
                  AND (? IS NULL OR ? = ''
                       OR full_name LIKE '%' || ? || '%'
                       OR phone LIKE '%' || ? || '%'
                       OR IFNULL(email, '') LIKE '%' || ? || '%'
                       OR blood_type LIKE '%' || ? || '%')
                ORDER BY full_name COLLATE NOCASE, id""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, activeOnly ? 1 : 0);
            boolean hasQ = query != null && !query.isBlank();
            String q = hasQ ? query.trim() : "";
            ps.setString(2, hasQ ? q : null);
            ps.setString(3, hasQ ? q : null);
            ps.setString(4, hasQ ? q : null);
            ps.setString(5, hasQ ? q : null);
            ps.setString(6, hasQ ? q : null);
            ps.setString(7, hasQ ? q : null);
            try (ResultSet rs = ps.executeQuery()) {
                List<Donor> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }
        }
    }

    public int insert(Connection connection, String fullName, String phone, String email, String bloodType,
                      LocalDate dateOfBirth) throws SQLException {
        String sql = """
                INSERT INTO donors (full_name, phone, email, blood_type, date_of_birth, last_donation_date,
                                    is_active, created_at)
                VALUES (?, ?, ?, ?, ?, NULL, 1, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setString(4, bloodType);
            ps.setString(5, dateOfBirth.toString());
            ps.setString(6, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void update(Connection connection, int id, String fullName, String phone, String email, String bloodType,
                       LocalDate dateOfBirth) throws SQLException {
        String sql = """
                UPDATE donors
                SET full_name = ?, phone = ?, email = ?, blood_type = ?, date_of_birth = ?
                WHERE id = ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setString(4, bloodType);
            ps.setString(5, dateOfBirth.toString());
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void setActive(Connection connection, int id, boolean active) throws SQLException {
        String sql = "UPDATE donors SET is_active = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Recomputes {@code last_donation_date} from the latest non-voided donation for the donor.
     */
    public void refreshLastDonationDateFromDonations(Connection connection, int donorId) throws SQLException {
        String sql = """
                UPDATE donors
                SET last_donation_date = (
                    SELECT MAX(donation_date)
                    FROM donations
                    WHERE donor_id = ? AND is_voided = 0
                )
                WHERE id = ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, donorId);
            ps.setInt(2, donorId);
            ps.executeUpdate();
        }
    }

    private static Donor mapRow(ResultSet rs) throws SQLException {
        String last = rs.getString("last_donation_date");
        LocalDate lastDonation = last == null || last.isBlank() ? null : LocalDate.parse(last);
        String email = rs.getString("email");
        return new Donor(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                email,
                rs.getString("blood_type"),
                LocalDate.parse(rs.getString("date_of_birth")),
                lastDonation,
                rs.getInt("is_active") == 1,
                rs.getString("created_at")
        );
    }
}
