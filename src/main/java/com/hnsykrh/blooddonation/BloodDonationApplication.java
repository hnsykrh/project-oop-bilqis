package com.hnsykrh.blooddonation;

import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.view.LoginDialog;
import com.hnsykrh.blooddonation.view.MainFrame;
import com.hnsykrh.blooddonation.view.SessionContext;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Desktop entry point. Initializes persistence, then opens the Swing GUI (MVC View layer).
 */
public final class BloodDonationApplication {

    private BloodDonationApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to default LAF
            }
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.initialize();
            LoginDialog login = new LoginDialog(null, databaseManager);
            login.setVisible(true);
            SessionContext session = login.getSession();
            if (session != null) {
                MainFrame frame = new MainFrame(databaseManager, session);
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
