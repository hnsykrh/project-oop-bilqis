package com.hnsykrh.blooddonation.controller;

import com.hnsykrh.blooddonation.dao.BloodInventoryDao;
import com.hnsykrh.blooddonation.dao.DonationDao;
import com.hnsykrh.blooddonation.dao.DonorDao;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.model.Donation;
import com.hnsykrh.blooddonation.model.Donor;
import com.hnsykrh.blooddonation.service.EligibilityCalculator;
import com.hnsykrh.blooddonation.service.ServiceException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DonationController {

    private final DatabaseManager databaseManager;
    private final DonationDao donationDao = new DonationDao();
    private final DonorDao donorDao = new DonorDao();
    private final BloodInventoryDao inventoryDao = new BloodInventoryDao();

    public DonationController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Donation> search(String query, boolean nonVoidedOnly) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return donationDao.search(connection, query, nonVoidedOnly);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load donations: " + e.getMessage(), e);
        }
    }

    public Optional<Donation> findById(int id) throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return donationDao.findById(connection, id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load donation: " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> volumeByBloodType() throws ServiceException {
        try (var connection = databaseManager.openConnection()) {
            return donationDao.volumeByBloodType(connection);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load donation analytics: " + e.getMessage(), e);
        }
    }

    /**
     * Records a donation, updates donor last donation date, and increases inventory for the donor blood type.
     */
    public int recordDonation(int donorId, LocalDate donationDate, int volumeMl, double hemoglobinGdl, int staffId)
            throws ServiceException {
        if (volumeMl <= 0) {
            throw new ServiceException("Volume must be positive.");
        }
        try (var connection = databaseManager.openConnection()) {
            connection.setAutoCommit(false);
            try {
                Donor donor = donorDao.findById(connection, donorId)
                        .orElseThrow(() -> new ServiceException("Donor not found."));
                EligibilityCalculator.EligibilityResult eligibility =
                        EligibilityCalculator.evaluate(donor, donationDate, hemoglobinGdl);
                if (!eligibility.eligible()) {
                    throw new ServiceException(eligibility.message());
                }
                int donationId = donationDao.insert(connection, donorId, donationDate, volumeMl, hemoglobinGdl, staffId);
                donorDao.refreshLastDonationDateFromDonations(connection, donorId);
                inventoryDao.addStock(connection, donor.getBloodType(), volumeMl);
                connection.commit();
                return donationId;
            } catch (ServiceException e) {
                connection.rollback();
                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw new ServiceException("Failed to record donation: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Voids a donation, reverses inventory, and refreshes donor last donation date from remaining donations.
     */
    public void voidDonation(int donationId, String voidReason) throws ServiceException {
        if (voidReason == null || voidReason.isBlank()) {
            throw new ServiceException("Void reason is required.");
        }
        try (var connection = databaseManager.openConnection()) {
            connection.setAutoCommit(false);
            try {
                Donation donation = donationDao.findById(connection, donationId)
                        .orElseThrow(() -> new ServiceException("Donation not found."));
                if (donation.isVoided()) {
                    throw new ServiceException("Donation is already voided.");
                }
                donationDao.voidDonation(connection, donationId, voidReason.trim());
                inventoryDao.addStock(connection, donation.getDonorBloodType(), -donation.getVolumeMl());
                donorDao.refreshLastDonationDateFromDonations(connection, donation.getDonorId());
                connection.commit();
            } catch (ServiceException e) {
                connection.rollback();
                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw new ServiceException("Failed to void donation: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Database error: " + e.getMessage(), e);
        }
    }
}
