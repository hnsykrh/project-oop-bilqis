package com.hnsykrh.blooddonation.fx;

import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.view.SessionContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point (default GUI). Swing remains available via {@link com.hnsykrh.blooddonation.BloodDonationApplication}.
 */
public final class BloodDonationFxApplication extends Application {

    private static DatabaseManager databaseManager;

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Override
    public void start(Stage stage) throws Exception {
        databaseManager = new DatabaseManager();
        databaseManager.initialize();

        FXMLLoader loader = new FXMLLoader(
                BloodDonationFxApplication.class.getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setStage(stage);

        stage.setTitle("Blood Donation Management System — Login");
        stage.setScene(new Scene(root, 420, 260));
        stage.setResizable(false);
        stage.show();
    }

    static void openMainWindow(SessionContext session, Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                BloodDonationFxApplication.class.getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.initialize(new FxAppContext(databaseManager, session), stage);

        stage.setTitle("Blood Donation Management System");
        stage.setScene(new Scene(root, 1020, 680));
        stage.setResizable(true);
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
