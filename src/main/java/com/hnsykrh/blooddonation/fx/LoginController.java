package com.hnsykrh.blooddonation.fx;

import com.hnsykrh.blooddonation.controller.AuthController;
import com.hnsykrh.blooddonation.service.ServiceException;
import com.hnsykrh.blooddonation.view.SessionContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * JavaFX View controller for login (FXML). Calls {@link AuthController} only.
 */
public final class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private Stage stage;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onLogin() {
        AuthController auth = new AuthController(BloodDonationFxApplication.getDatabaseManager());
        try {
            Optional<SessionContext> session = auth.login(usernameField.getText(), passwordField.getText());
            if (session.isPresent()) {
                try {
                    BloodDonationFxApplication.openMainWindow(session.get(), stage);
                } catch (Exception ex) {
                    FxUtil.showError(stage, ex.getMessage() != null ? ex.getMessage() : "Failed to open main window.");
                }
            } else {
                FxUtil.showError(stage, "Invalid username or password.");
            }
        } catch (ServiceException ex) {
            FxUtil.showError(stage, ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        Platform.exit();
    }
}
