package com.hnsykrh.blooddonation.fx;

import com.hnsykrh.blooddonation.fx.tabs.AnalyticsTabFx;
import com.hnsykrh.blooddonation.fx.tabs.DonationsTabFx;
import com.hnsykrh.blooddonation.fx.tabs.DonorsTabFx;
import com.hnsykrh.blooddonation.fx.tabs.InventoryTabFx;
import com.hnsykrh.blooddonation.fx.tabs.RequestsTabFx;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * JavaFX View controller for main window (FXML). Wires tab views to {@link FxAppContext}.
 */
public final class MainController {

    @FXML
    private Label statusLabel;
    @FXML
    private TabPane tabPane;

    private Stage stage;

    void initialize(FxAppContext context, Stage stage) {
        this.stage = stage;
        statusLabel.setText("Signed in: " + context.getSession().getFullName()
                + " (" + context.getSession().getUsername() + ", " + context.getSession().getRole() + ")");

        tabPane.getTabs().add(new Tab("Donors", DonorsTabFx.build(context, stage)));
        tabPane.getTabs().add(new Tab("Donations", DonationsTabFx.build(context, stage)));
        tabPane.getTabs().add(new Tab("Inventory", InventoryTabFx.build(context, stage)));
        tabPane.getTabs().add(new Tab("Recipient Requests", RequestsTabFx.build(context, stage)));
        tabPane.getTabs().add(new Tab("Analytics", AnalyticsTabFx.build(context, stage)));
    }
}
