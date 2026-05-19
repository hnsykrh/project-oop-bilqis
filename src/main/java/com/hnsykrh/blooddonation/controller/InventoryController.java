package com.hnsykrh.blooddonation.controller;

import com.hnsykrh.blooddonation.dao.BloodInventoryDao;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.model.BloodInventory;
import com.hnsykrh.blooddonation.service.ServiceException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class InventoryController {

    private final DatabaseManager databaseManager;
    private final BloodInventoryDao inventoryDao = new BloodInventoryDao();

    public InventoryController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<BloodInventory> listAll() throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return inventoryDao.findAll(connection);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load inventory: " + e.getMessage(), e);
        }
    }

    public Optional<BloodInventory> findByBloodType(String bloodType) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return inventoryDao.findByBloodType(connection, bloodType);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load inventory row: " + e.getMessage(), e);
        }
    }
}
