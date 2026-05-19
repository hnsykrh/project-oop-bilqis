package com.hnsykrh.blooddonation;

import com.hnsykrh.blooddonation.controller.AuthController;
import com.hnsykrh.blooddonation.controller.DonationController;
import com.hnsykrh.blooddonation.controller.DonorController;
import com.hnsykrh.blooddonation.controller.InventoryController;
import com.hnsykrh.blooddonation.controller.ReportController;
import com.hnsykrh.blooddonation.controller.RequestController;
import com.hnsykrh.blooddonation.dao.BloodInventoryDao;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.model.Donor;
import com.hnsykrh.blooddonation.service.ServiceException;
import com.hnsykrh.blooddonation.support.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationWorkflowTest {

    @TempDir
    Path tempDir;

    private DatabaseManager databaseManager;
    private DonorController donorController;
    private DonationController donationController;
    private InventoryController inventoryController;
    private RequestController requestController;
    private AuthController authController;
    private ReportController reportController;
    private final BloodInventoryDao inventoryDao = new BloodInventoryDao();

    @BeforeEach
    void setUp() throws Exception {
        Path db = tempDir.resolve("test.db");
        databaseManager = new DatabaseManager(db);
        databaseManager.initialize(false);
        donorController = new DonorController(databaseManager);
        donationController = new DonationController(databaseManager);
        inventoryController = new InventoryController(databaseManager);
        requestController = new RequestController(databaseManager);
        authController = new AuthController(databaseManager);
        reportController = new ReportController(databaseManager);
    }

    @Test
    void loginAndCrudSoftDeleteSearchView() throws Exception {
        assertTrue(authController.login("admin", "admin123").isPresent());
        assertTrue(authController.login("admin", "wrong").isEmpty());

        int donorId = donorController.create("Ali", "011", "a@b.com", "O+", LocalDate.of(1995, 3, 3));
        Donor created = donorController.findById(donorId).orElseThrow();
        assertEquals("Ali", created.getFullName());

        List<Donor> search = donorController.search("Ali", true);
        assertEquals(1, search.size());

        donorController.update(donorId, "Ali Updated", "011", "a@b.com", "O+", LocalDate.of(1995, 3, 3));
        assertEquals("Ali Updated", donorController.findById(donorId).orElseThrow().getFullName());

        donorController.deactivate(donorId);
        assertFalse(donorController.findById(donorId).orElseThrow().isActive());
        assertTrue(donorController.search("Ali", true).isEmpty());
        assertEquals(1, donorController.search("Ali", false).size());
    }

    @Test
    void donationUpdatesInventoryAndVoidBlockedWhenBloodIssued() throws Exception {
        int donorId = donorController.create("Donor", "012", null, "A+", LocalDate.of(1998, 1, 1));
        int donationId = donationController.recordDonation(donorId, LocalDate.now(), 450, 13.5, 1);

        try (var c = databaseManager.openConnection()) {
            assertEquals(450, inventoryDao.getStockMl(c, "A+"));
        }

        int requestId = requestController.create("P1", "Hospital", "A+", 500, LocalDate.now(), "urgent");
        int fulfilled = requestController.fulfill(requestId, 400);
        assertEquals(400, fulfilled);

        try (var c = databaseManager.openConnection()) {
            assertEquals(50, inventoryDao.getStockMl(c, "A+"));
        }

        ServiceException ex = assertThrows(ServiceException.class,
                () -> donationController.voidDonation(donationId, "lab error"));
        assertTrue(ex.getMessage().contains("Cannot void"));

        try (var c = databaseManager.openConnection()) {
            assertEquals(50, inventoryDao.getStockMl(c, "A+"));
        }
    }

    @Test
    void voidDonationReversesStockWhenNotIssued() throws Exception {
        int donorId = donorController.create("Donor2", "013", null, "B+", LocalDate.of(1998, 6, 6));
        int donationId = donationController.recordDonation(donorId, LocalDate.now(), 450, 14.0, 1);
        donationController.voidDonation(donationId, "duplicate entry");
        try (var c = databaseManager.openConnection()) {
            assertEquals(0, inventoryDao.getStockMl(c, "B+"));
        }
    }

    @Test
    void analyticsAndPdfExport() throws Exception {
        int donorId = donorController.create("Chart", "014", null, "O-", LocalDate.of(1997, 7, 7));
        donationController.recordDonation(donorId, LocalDate.now(), 450, 13.0, 1);
        assertEquals(450, donationController.volumeByBloodType().getOrDefault("O-", 0));

        Path pdf = tempDir.resolve("report.pdf");
        reportController.exportInventoryPdf(pdf);
        assertTrue(Files.size(pdf) > 100);
    }

    @Test
    void eligibilityBlocksTooSoonDonation() throws Exception {
        int donorId = donorController.create("Recent", "015", null, "AB+", LocalDate.of(1999, 9, 9));
        donationController.recordDonation(donorId, LocalDate.now().minusDays(30), 450, 13.0, 1);
        assertThrows(ServiceException.class,
                () -> donationController.recordDonation(donorId, LocalDate.now(), 450, 13.0, 1));
    }
}
