package com.hnsykrh.blooddonation.dao;

import com.hnsykrh.blooddonation.model.Staff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StaffDao {

    public Optional<Staff> findActiveByCredentials(Connection connection, String username, String passwordPlain)
            throws SQLException {
        String sql = """
                SELECT id, username, password_plain, full_name, role, is_active, created_at
                FROM staff
                WHERE username = ? AND password_plain = ? AND is_active = 1""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordPlain);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    public Optional<Staff> findById(Connection connection, int id) throws SQLException {
        String sql = """
                SELECT id, username, password_plain, full_name, role, is_active, created_at
                FROM staff WHERE id = ?""";
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

    public List<Staff> search(Connection connection, String query, boolean activeOnly) throws SQLException {
        String sql = """
                SELECT id, username, password_plain, full_name, role, is_active, created_at
                FROM staff
                WHERE (? = 0 OR is_active = 1)
                  AND (? IS NULL OR ? = ''
                       OR username LIKE '%' || ? || '%'
                       OR full_name LIKE '%' || ? || '%'
                       OR role LIKE '%' || ? || '%')
                ORDER BY username COLLATE NOCASE""";
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
                List<Staff> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }
        }
    }

    public int insert(Connection connection, String username, String passwordPlain, String fullName, String role,
                      boolean active) throws SQLException {
        String sql = """
                INSERT INTO staff (username, password_plain, full_name, role, is_active, created_at)
                VALUES (?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordPlain);
            ps.setString(3, fullName);
            ps.setString(4, role);
            ps.setInt(5, active ? 1 : 0);
            ps.setString(6, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void update(Connection connection, int id, String username, String passwordPlain, String fullName,
                       String role, boolean active) throws SQLException {
        String sql = """
                UPDATE staff
                SET username = ?, password_plain = ?, full_name = ?, role = ?, is_active = ?
                WHERE id = ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordPlain);
            ps.setString(3, fullName);
            ps.setString(4, role);
            ps.setInt(5, active ? 1 : 0);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void setActive(Connection connection, int id, boolean active) throws SQLException {
        String sql = "UPDATE staff SET is_active = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private static Staff mapRow(ResultSet rs) throws SQLException {
        return new Staff(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_plain"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getInt("is_active") == 1,
                rs.getString("created_at")
        );
    }
}
