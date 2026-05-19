package com.hnsykrh.blooddonation.fx.tabs;

import com.hnsykrh.blooddonation.fx.FxAppContext;
import com.hnsykrh.blooddonation.fx.FxUtil;
import com.hnsykrh.blooddonation.model.RecipientRequest;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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

public final class RequestsTabFx {

    private RequestsTabFx() {
    }

    public static BorderPane build(FxAppContext ctx, Stage stage) {
        BorderPane root = new BorderPane();
        TextField searchField = new TextField();
        searchField.setPrefWidth(180);
        CheckBox openOnly = new CheckBox("Open only");
        Button searchBtn = new Button("Search");
        Button refreshBtn = new Button("Refresh");
        Button viewBtn = new Button("View");
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button fulfillBtn = new Button("Fulfill");
        Button cancelBtn = new Button("Cancel");

        HBox toolbar = new HBox(8, new Label("Search:"), searchField, openOnly,
                searchBtn, refreshBtn, viewBtn, addBtn, updateBtn, fulfillBtn, cancelBtn);
        toolbar.setPadding(new Insets(8));

        TableView<RequestRow> table = new TableView<>(FXCollections.observableArrayList());
        table.getColumns().add(col("ID", "id"));
        table.getColumns().add(col("Patient", "patient"));
        table.getColumns().add(col("Hospital", "hospital"));
        table.getColumns().add(col("Blood", "blood"));
        table.getColumns().add(col("Remaining", "remaining"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Runnable reload = () -> FxUtil.runAsync(stage, () -> ctx.getRequestController().search(
                searchField.getText().trim(), openOnly.isSelected()), list ->
                table.getItems().setAll(list.stream().map(RequestRow::from).toList()));

        searchBtn.setOnAction(e -> reload.run());
        refreshBtn.setOnAction(e -> reload.run());
        viewBtn.setOnAction(e -> {
            RequestRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a request row first.");
                return;
            }
            try {
                RecipientRequest r = ctx.getRequestController().findById(row.id()).orElse(null);
                if (r == null) {
                    FxUtil.showError(stage, "Request not found.");
                    return;
                }
                FxUtil.showInfo(stage, """
                        ID: %d | Patient: %s | Hospital: %s
                        Blood: %s | Needed: %d mL | Fulfilled: %d mL | Remaining: %d mL
                        Date: %s | Cancelled: %s""".formatted(
                        r.getId(), r.getPatientReference(), r.getHospitalName(), r.getBloodType(),
                        r.getUnitsNeededMl(), r.getFulfilledMl(), r.remainingMl(), r.getRequestDate(),
                        r.isCancelled() ? "Yes" : "No"));
            } catch (Exception ex) {
                FxUtil.showError(stage, ex.getMessage());
            }
        });
        addBtn.setOnAction(e -> {
            RequestForm form = RequestForm.prompt(stage, null);
            if (form != null) {
                FxUtil.runAsync(stage, () -> {
                    ctx.getRequestController().create(form.patient(), form.hospital(), form.bloodType(),
                            form.units(), form.date(), form.notes());
                    return null;
                }, ignored -> reload.run());
            }
        });
        updateBtn.setOnAction(e -> {
            RequestRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a request row first.");
                return;
            }
            try {
                RecipientRequest existing = ctx.getRequestController().findById(row.id()).orElse(null);
                if (existing == null) {
                    FxUtil.showError(stage, "Request not found.");
                    return;
                }
                RequestForm form = RequestForm.prompt(stage, existing);
                if (form != null) {
                    FxUtil.runAsync(stage, () -> {
                        ctx.getRequestController().update(row.id(), form.patient(), form.hospital(),
                                form.bloodType(), form.units(), form.date(), form.notes());
                        return null;
                    }, ignored -> reload.run());
                }
            } catch (Exception ex) {
                FxUtil.showError(stage, ex.getMessage());
            }
        });
        fulfillBtn.setOnAction(e -> {
            RequestRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a request row first.");
                return;
            }
            TextField amount = new TextField("250");
            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(8);
            grid.addRow(0, new Label("Amount (mL)"), amount);
            if (!dialogOk(stage, "Fulfill request #" + row.id(), grid)) {
                return;
            }
            try {
                int requested = Integer.parseInt(amount.getText().trim());
                FxUtil.runAsync(stage, () -> ctx.getRequestController().fulfill(row.id(), requested),
                        actual -> {
                            FxUtil.showInfo(stage, "Fulfilled " + actual + " mL.");
                            reload.run();
                        });
            } catch (NumberFormatException ex) {
                FxUtil.showError(stage, "Invalid amount.");
            }
        });
        cancelBtn.setOnAction(e -> {
            RequestRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a request row first.");
                return;
            }
            if (FxUtil.confirm(stage, "Cancel (soft-delete) request #" + row.id() + "?")) {
                FxUtil.runAsync(stage, () -> {
                    ctx.getRequestController().cancel(row.id());
                    return null;
                }, ignored -> reload.run());
            }
        });

        root.setTop(toolbar);
        root.setCenter(table);
        reload.run();
        return root;
    }

    private static TableColumn<RequestRow, String> col(String title, String prop) {
        TableColumn<RequestRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }

    private static boolean dialogOk(Stage stage, String title, GridPane grid) {
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        dialog.initOwner(stage);
        return dialog.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                == javafx.scene.control.ButtonType.OK;
    }

    public record RequestRow(int id, String patient, String hospital, String blood, String remaining) {
        static RequestRow from(RecipientRequest r) {
            return new RequestRow(r.getId(), r.getPatientReference(), r.getHospitalName(),
                    r.getBloodType(), String.valueOf(r.remainingMl()));
        }
    }

    private record RequestForm(String patient, String hospital, String bloodType, int units, LocalDate date,
                               String notes) {
        static RequestForm prompt(Stage stage, RecipientRequest existing) {
            TextField patient = new TextField(existing != null ? existing.getPatientReference() : "");
            TextField hospital = new TextField(existing != null ? existing.getHospitalName() : "");
            ComboBox<String> blood = new ComboBox<>(FXCollections.observableArrayList(FxUtil.BLOOD_TYPES));
            if (existing != null) {
                blood.getSelectionModel().select(existing.getBloodType());
            } else {
                blood.getSelectionModel().selectFirst();
            }
            TextField units = new TextField(existing != null ? String.valueOf(existing.getUnitsNeededMl()) : "500");
            TextField date = new TextField(existing != null ? existing.getRequestDate().toString()
                    : LocalDate.now().toString());
            TextField notes = new TextField(existing != null && existing.getNotes() != null ? existing.getNotes() : "");
            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(8);
            grid.addRow(0, new Label("Patient"), patient);
            grid.addRow(1, new Label("Hospital"), hospital);
            grid.addRow(2, new Label("Blood type"), blood);
            grid.addRow(3, new Label("Units needed (mL)"), units);
            grid.addRow(4, new Label("Request date"), date);
            grid.addRow(5, new Label("Notes"), notes);
            if (!dialogOk(stage, existing == null ? "Add request" : "Update request", grid)) {
                return null;
            }
            try {
                return new RequestForm(patient.getText().trim(), hospital.getText().trim(), blood.getValue(),
                        Integer.parseInt(units.getText().trim()), LocalDate.parse(date.getText().trim()),
                        notes.getText().trim());
            } catch (DateTimeParseException | NumberFormatException ex) {
                FxUtil.showError(stage, "Invalid date or units.");
                return null;
            }
        }
    }
}
