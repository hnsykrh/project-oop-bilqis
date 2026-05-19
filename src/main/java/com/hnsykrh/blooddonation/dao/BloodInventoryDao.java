package com.hnsykrh.blooddonation.dao;

import com.hnsykrh.blooddonation.model.BloodInventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BloodInventoryDao {

    public List<BloodInventory> findAll(Connection connection) throws SQLException {
        String sql = """
                SELECT id, blood_type, stock_ml, updated_at
                FROM blood_inventory
                ORDER BY blood_type""";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<BloodInventory> out = new ArrayList<>();
            while (rs.next()) {
                out.add(mapRow(rs));
            }
            return out;
        }
    }

    public Optional<BloodInventory> findByBloodType(Connection connection, String bloodType) throws SQLException {
        String sql = """
                SELECT id, blood_type, stock_ml, updated_at
                FROM blood_inventory WHERE blood_type = ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, bloodType);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    public void addStock(Connection connection, String bloodType, int deltaMl) throws SQLException {
        String sql = """
                UPDATE blood_inventory
                SET stock_ml = stock_ml + ?, updated_at = ?
                WHERE blood_type = ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, deltaMl);
            ps.setString(2, Instant.now().toString());
            ps.setString(3, bloodType);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Unknown blood type in inventory: " + bloodType);
            }
        }
    }

    /**
     * Subtracts stock when fulfilling a request. Caller must ensure sufficient stock.
     */
    public void subtractStock(Connection connection, String bloodType, int deltaMl) throws SQLException {
        if (deltaMl <= 0) {
            return;
        }
        String sql = """
                UPDATE blood_inventory
                SET stock_ml = stock_ml - ?, updated_at = ?
                WHERE blood_type = ? AND stock_ml >= ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, deltaMl);
            ps.setString(2, Instant.now().toString());
            ps.setString(3, bloodType);
            ps.setInt(4, deltaMl);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Insufficient stock for " + bloodType);
            }
        }
    }

    private static BloodInventory mapRow(ResultSet rs) throws SQLException {
        return new BloodInventory(
                rs.getInt("id"),
                rs.getString("blood_type"),
                rs.getInt("stock_ml"),
                rs.getString("updated_at")
        );
    }
}
