package com.hnsykrh.blooddonation.view;

import com.hnsykrh.blooddonation.controller.DonationController;
import com.hnsykrh.blooddonation.controller.DonorController;
import com.hnsykrh.blooddonation.controller.InventoryController;
import com.hnsykrh.blooddonation.controller.ReportController;
import com.hnsykrh.blooddonation.controller.RequestController;
import com.hnsykrh.blooddonation.db.DatabaseManager;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;

public final class MainFrame extends JFrame {

    public MainFrame(DatabaseManager databaseManager, SessionContext session) {
        super("Blood Donation Management System");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);

        DonorController donorController = new DonorController(databaseManager);
        DonationController donationController = new DonationController(databaseManager);
        InventoryController inventoryController = new InventoryController(databaseManager);
        RequestController requestController = new RequestController(databaseManager);
        ReportController reportController = new ReportController(databaseManager);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Donors", new DonorsTabPanel(donorController));
        tabs.addTab("Donations", new DonationsTabPanel(donationController, donorController, session));
        tabs.addTab("Inventory", new InventoryTabPanel(inventoryController, reportController));
        tabs.addTab("Recipient Requests", new RequestsTabPanel(requestController));
        tabs.addTab("Analytics", new AnalyticsTabPanel(donationController));

        JLabel status = new JLabel("Signed in: " + session.getFullName() + " (" + session.getUsername() + ", "
                + session.getRole() + ")");
        getContentPane().add(status, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
    }
}
