package com.hnsykrh.blooddonation.fx;

import com.hnsykrh.blooddonation.controller.DonationController;
import com.hnsykrh.blooddonation.controller.DonorController;
import com.hnsykrh.blooddonation.controller.InventoryController;
import com.hnsykrh.blooddonation.controller.ReportController;
import com.hnsykrh.blooddonation.controller.RequestController;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.view.SessionContext;

/**
 * Shared MVC wiring for JavaFX: business controllers + session (View layer helpers call these only).
 */
public final class FxAppContext {

    private final DatabaseManager databaseManager;
    private final SessionContext session;
    private final DonorController donorController;
    private final DonationController donationController;
    private final InventoryController inventoryController;
    private final RequestController requestController;
    private final ReportController reportController;

    public FxAppContext(DatabaseManager databaseManager, SessionContext session) {
        this.databaseManager = databaseManager;
        this.session = session;
        this.donorController = new DonorController(databaseManager);
        this.donationController = new DonationController(databaseManager);
        this.inventoryController = new InventoryController(databaseManager);
        this.requestController = new RequestController(databaseManager);
        this.reportController = new ReportController(databaseManager);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SessionContext getSession() {
        return session;
    }

    public DonorController getDonorController() {
        return donorController;
    }

    public DonationController getDonationController() {
        return donationController;
    }

    public InventoryController getInventoryController() {
        return inventoryController;
    }

    public RequestController getRequestController() {
        return requestController;
    }

    public ReportController getReportController() {
        return reportController;
    }
}
