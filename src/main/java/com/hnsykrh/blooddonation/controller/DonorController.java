package com.hnsykrh.blooddonation.controller;

import com.hnsykrh.blooddonation.dao.DonorDao;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.model.Donor;
import com.hnsykrh.blooddonation.service.EligibilityCalculator;
import com.hnsykrh.blooddonation.service.ServiceException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class DonorController {

    private final DatabaseManager databaseManager;
    private final DonorDao donorDao = new DonorDao();

    public DonorController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Donor> search(String query, boolean activeOnly) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return donorDao.search(connection, query, activeOnly);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load donors: " + e.getMessage(), e);
        }
    }

    public Optional<Donor> findById(int id) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return donorDao.findById(connection, id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load donor: " + e.getMessage(), e);
        }
    }

    public int create(String fullName, String phone, String email, String bloodType, LocalDate dateOfBirth)
            throws ServiceException {
        validateDonorFields(fullName, phone, bloodType, dateOfBirth);
        try (var connection = databaseManager.openConnection()) {
            return donorDao.insert(connection, fullName.trim(), phone.trim(), blankToNull(email),
                    bloodType.trim(), dateOfBirth);
        } catch (SQLException e) {
            throw new ServiceException("Failed to create donor: " + e.getMessage(), e);
        }
    }

    public void update(int id, String fullName, String phone, String email, String bloodType, LocalDate dateOfBirth)
            throws ServiceException {
        validateDonorFields(fullName, phone, bloodType, dateOfBirth);
        try (var connection = databaseManager.openConnection()) {
            donorDao.update(connection, id, fullName.trim(), phone.trim(), blankToNull(email),
                    bloodType.trim(), dateOfBirth);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update donor: " + e.getMessage(), e);
        }
    }

    public void deactivate(int id) throws ServiceException {
        setActive(id, false);
    }

    public void reactivate(int id) throws ServiceException {
        setActive(id, true);
    }

    public long daysUntilEligible(int donorId, LocalDate asOf) throws ServiceException {
        Donor donor = findById(donorId).orElseThrow(() -> new ServiceException("Donor not found."));
        return EligibilityCalculator.daysUntilNextEligible(donor, asOf);
    }

    private void setActive(int id, boolean active) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            donorDao.setActive(connection, id, active);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update donor status: " + e.getMessage(), e);
        }
    }

    private static void validateDonorFields(String fullName, String phone, String bloodType, LocalDate dateOfBirth)
            throws ServiceException {
        if (fullName == null || fullName.isBlank()) {
            throw new ServiceException("Full name is required.");
        }
        if (phone == null || phone.isBlank()) {
            throw new ServiceException("Phone is required.");
        }
        if (bloodType == null || bloodType.isBlank()) {
            throw new ServiceException("Blood type is required.");
        }
        if (dateOfBirth == null) {
            throw new ServiceException("Date of birth is required.");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
