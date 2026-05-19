package com.hnsykrh.blooddonation.fx.tabs;

import com.hnsykrh.blooddonation.fx.FxAppContext;
import com.hnsykrh.blooddonation.fx.FxUtil;
import com.hnsykrh.blooddonation.model.Donation;
import com.hnsykrh.blooddonation.model.Donor;
import com.hnsykrh.blooddonation.service.EligibilityCalculator;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class DonationsTabFx {

    private DonationsTabFx() {
    }

    public static BorderPane build(FxAppContext ctx, Stage stage) {
        BorderPane root = new BorderPane();
        TextField searchField = new TextField();
        searchField.setPrefWidth(180);
        CheckBox nonVoidedOnly = new CheckBox("Non-voided only");
        Button searchBtn = new Button("Search");
        Button refreshBtn = new Button("Refresh");
        Button viewBtn = new Button("View");
        Button recordBtn = new Button("Record donation");
        Button voidBtn = new Button("Void");

        HBox toolbar = new HBox(8, new Label("Search:"), searchField, nonVoidedOnly,
                searchBtn, refreshBtn, viewBtn, recordBtn, voidBtn);
        toolbar.setPadding(new Insets(8));

        TableView<DonationRow> table = new TableView<>(FXCollections.observableArrayList());
        table.getColumns().add(column("ID", "id"));
        table.getColumns().add(column("Donor", "donorName"));
        table.getColumns().add(column("Blood", "bloodType"));
        table.getColumns().add(column("Date", "date"));
        table.getColumns().add(column("Volume", "volume"));
        table.getColumns().add(column("Voided", "voided"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Runnable reload = () -> FxUtil.runAsync(stage, () -> ctx.getDonationController().search(
                searchField.getText().trim(), nonVoidedOnly.isSelected()), list ->
                table.getItems().setAll(list.stream().map(DonationRow::from).toList()));

        searchBtn.setOnAction(e -> reload.run());
        refreshBtn.setOnAction(e -> reload.run());
        viewBtn.setOnAction(e -> {
            DonationRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a donation row first.");
                return;
            }
            try {
                Donation d = ctx.getDonationController().findById(row.id()).orElse(null);
                if (d == null) {
                    FxUtil.showError(stage, "Donation not found.");
                    return;
                }
                FxUtil.showInfo(stage, """
                        ID: %d | Donor: %s (#%d) | Blood: %s
                        Date: %s | Volume: %d mL | Hb: %.1f g/dL
                        Voided: %s | Reason: %s""".formatted(
                        d.getId(), d.getDonorName(), d.getDonorId(), d.getDonorBloodType(),
                        d.getDonationDate(), d.getVolumeMl(), d.getHemoglobinGdl(),
                        d.isVoided() ? "Yes" : "No",
                        d.getVoidReason() == null ? "—" : d.getVoidReason()));
            } catch (Exception ex) {
                FxUtil.showError(stage, ex.getMessage());
            }
        });
        recordBtn.setOnAction(e -> {
            TextField donorId = new TextField();
            TextField date = new TextField(LocalDate.now().toString());
            TextField volume = new TextField("450");
            TextField hb = new TextField("13.5");
            GridPane grid = formGrid(donorId, date, volume, hb);
            if (!okDialog(stage, "Record donation", grid)) {
                return;
            }
            try {
                int id = Integer.parseInt(donorId.getText().trim());
                LocalDate donationDate = LocalDate.parse(date.getText().trim());
                int vol = Integer.parseInt(volume.getText().trim());
                double hemoglobin = Double.parseDouble(hb.getText().trim());
                Donor donor = ctx.getDonorController().findById(id).orElse(null);
                if (donor == null) {
                    FxUtil.showError(stage, "Donor not found.");
                    return;
                }
                var check = EligibilityCalculator.evaluate(donor, donationDate, hemoglobin);
                if (!check.eligible()) {
                    FxUtil.showError(stage, check.message());
                    return;
                }
                FxUtil.runAsync(stage, () -> ctx.getDonationController().recordDonation(
                        id, donationDate, vol, hemoglobin, ctx.getSession().getStaffId()),
                        ignored -> reload.run());
            } catch (NumberFormatException | DateTimeParseException ex) {
                FxUtil.showError(stage, "Invalid numeric or date input.");
            } catch (Exception ex) {
                FxUtil.showError(stage, ex.getMessage());
            }
        });
        voidBtn.setOnAction(e -> {
            DonationRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a donation row first.");
                return;
            }
            TextField reason = new TextField();
            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(8);
            grid.addRow(0, new Label("Void reason"), reason);
            if (!okDialog(stage, "Void donation #" + row.id(), grid)) {
                return;
            }
            FxUtil.runAsync(stage, () -> {
                ctx.getDonationController().voidDonation(row.id(), reason.getText().trim());
                return null;
            }, ignored -> reload.run());
        });

        root.setTop(toolbar);
        root.setCenter(table);
        reload.run();
        return root;
    }

    private static TableColumn<DonationRow, ?> column(String title, String prop) {
        TableColumn<DonationRow, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        return col;
    }

    private static GridPane formGrid(TextField donorId, TextField date, TextField volume, TextField hb) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.addRow(0, new Label("Donor ID"), donorId);
        grid.addRow(1, new Label("Date (YYYY-MM-DD)"), date);
        grid.addRow(2, new Label("Volume (mL)"), volume);
        grid.addRow(3, new Label("Hemoglobin (g/dL)"), hb);
        return grid;
    }

    private static boolean okDialog(Stage stage, String title, GridPane grid) {
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        dialog.initOwner(stage);
        return dialog.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                == javafx.scene.control.ButtonType.OK;
    }

    public record DonationRow(int id, String donorName, String bloodType, String date, String volume, String voided) {
        static DonationRow from(Donation d) {
            return new DonationRow(d.getId(), d.getDonorName(), d.getDonorBloodType(),
                    d.getDonationDate().toString(), String.valueOf(d.getVolumeMl()),
                    d.isVoided() ? "Yes" : "No");
        }
    }
}
