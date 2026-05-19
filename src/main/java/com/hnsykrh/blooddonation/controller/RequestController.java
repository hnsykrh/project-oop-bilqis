package com.hnsykrh.blooddonation.controller;

import com.hnsykrh.blooddonation.dao.BloodInventoryDao;
import com.hnsykrh.blooddonation.dao.RecipientRequestDao;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.model.RecipientRequest;
import com.hnsykrh.blooddonation.service.FulfillmentCalculator;
import com.hnsykrh.blooddonation.service.ServiceException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class RequestController {

    private final DatabaseManager databaseManager;
    private final RecipientRequestDao requestDao = new RecipientRequestDao();
    private final BloodInventoryDao inventoryDao = new BloodInventoryDao();

    public RequestController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<RecipientRequest> search(String query, boolean openOnly) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return requestDao.search(connection, query, openOnly);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load requests: " + e.getMessage(), e);
        }
    }

    public Optional<RecipientRequest> findById(int id) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return requestDao.findById(connection, id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load request: " + e.getMessage(), e);
        }
    }

    public int create(String patientReference, String hospitalName, String bloodType, int unitsNeededMl,
                      LocalDate requestDate, String notes) throws ServiceException {
        validateRequestFields(patientReference, hospitalName, bloodType, unitsNeededMl, requestDate);
        try (var connection = databaseManager.openConnection()) {
            return requestDao.insert(connection, patientReference.trim(), hospitalName.trim(), bloodType.trim(),
                    unitsNeededMl, requestDate, blankToNull(notes));
        } catch (SQLException e) {
            throw new ServiceException("Failed to create request: " + e.getMessage(), e);
        }
    }

    public void update(int id, String patientReference, String hospitalName, String bloodType, int unitsNeededMl,
                       LocalDate requestDate, String notes) throws ServiceException {
        validateRequestFields(patientReference, hospitalName, bloodType, unitsNeededMl, requestDate);
        try (var connection = databaseManager.openConnection()) {
            requestDao.update(connection, id, patientReference.trim(), hospitalName.trim(), bloodType.trim(),
                    unitsNeededMl, requestDate, blankToNull(notes));
        } catch (SQLException e) {
            throw new ServiceException("Failed to update request: " + e.getMessage(), e);
        }
    }

    public void cancel(int id) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            requestDao.setCancelled(connection, id, true);
        } catch (SQLException e) {
            throw new ServiceException("Failed to cancel request: " + e.getMessage(), e);
        }
    }

    /**
     * Fulfills up to {@code requestedMl}, capped by stock and remaining clinical need.
     *
     * @return actual milliliters allocated
     */
    public int fulfill(int requestId, int requestedMl) throws ServiceException {
        if (requestedMl <= 0) {
            throw new ServiceException("Fulfillment amount must be positive.");
        }
        try (var connection = databaseManager.openConnection()) {
            connection.setAutoCommit(false);
            try {
                RecipientRequest request = requestDao.findById(connection, requestId)
                        .orElseThrow(() -> new ServiceException("Request not found."));
                if (request.isCancelled()) {
                    throw new ServiceException("Request is cancelled.");
                }
                int remaining = request.remainingMl();
                if (remaining <= 0) {
                    throw new ServiceException("Request is already fully fulfilled.");
                }
                var inventory = inventoryDao.findByBloodType(connection, request.getBloodType())
                        .orElseThrow(() -> new ServiceException("No inventory for " + request.getBloodType()));
                int cap = FulfillmentCalculator.maxFulfillableMl(inventory.getStockMl(), remaining);
                int actual = Math.min(requestedMl, cap);
                if (actual <= 0) {
                    throw new ServiceException("Cannot fulfill: insufficient stock or no remaining need.");
                }
                inventoryDao.subtractStock(connection, request.getBloodType(), actual);
                requestDao.addFulfilledMl(connection, requestId, actual);
                connection.commit();
                return actual;
            } catch (ServiceException e) {
                connection.rollback();
                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw new ServiceException("Failed to fulfill request: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Database error: " + e.getMessage(), e);
        }
    }

    private static void validateRequestFields(String patientReference, String hospitalName, String bloodType,
                                              int unitsNeededMl, LocalDate requestDate) throws ServiceException {
        if (patientReference == null || patientReference.isBlank()) {
            throw new ServiceException("Patient reference is required.");
        }
        if (hospitalName == null || hospitalName.isBlank()) {
            throw new ServiceException("Hospital name is required.");
        }
        if (bloodType == null || bloodType.isBlank()) {
            throw new ServiceException("Blood type is required.");
        }
        if (unitsNeededMl <= 0) {
            throw new ServiceException("Units needed must be positive.");
        }
        if (requestDate == null) {
            throw new ServiceException("Request date is required.");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
