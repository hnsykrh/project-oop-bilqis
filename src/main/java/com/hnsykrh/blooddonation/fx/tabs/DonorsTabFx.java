package com.hnsykrh.blooddonation.fx.tabs;

import com.hnsykrh.blooddonation.fx.FxAppContext;
import com.hnsykrh.blooddonation.fx.FxUtil;
import com.hnsykrh.blooddonation.model.Donor;
import com.hnsykrh.blooddonation.service.EligibilityCalculator;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class DonorsTabFx {

    private DonorsTabFx() {
    }

    public static BorderPane build(FxAppContext ctx, Stage stage) {
        BorderPane root = new BorderPane();
        TextField searchField = new TextField();
        searchField.setPromptText("Search name, phone, blood type...");
        searchField.setPrefWidth(200);
        CheckBox activeOnly = new CheckBox("Active only");
        activeOnly.setSelected(true);
        Button searchBtn = new Button("Search");
        Button refreshBtn = new Button("Refresh");
        Button viewBtn = new Button("View");
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deactivateBtn = new Button("Deactivate");

        HBox toolbar = new HBox(8, new Label("Search:"), searchField, activeOnly,
                searchBtn, refreshBtn, viewBtn, addBtn, updateBtn, deactivateBtn);
        toolbar.setPadding(new Insets(8));

        TableView<DonorRow> table = new TableView<>(FXCollections.observableArrayList());
        TableColumn<DonorRow, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<DonorRow, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        TableColumn<DonorRow, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<DonorRow, String> colBlood = new TableColumn<>("Blood");
        colBlood.setCellValueFactory(new PropertyValueFactory<>("bloodType"));
        TableColumn<DonorRow, String> colActive = new TableColumn<>("Active");
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        table.getColumns().addAll(colId, colName, colPhone, colBlood, colActive);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Runnable reload = () -> FxUtil.runAsync(stage, () -> ctx.getDonorController().search(
                searchField.getText().trim(), activeOnly.isSelected()), donors -> {
            table.getItems().setAll(donors.stream().map(DonorRow::from).toList());
        });

        searchBtn.setOnAction(e -> reload.run());
        refreshBtn.setOnAction(e -> reload.run());
        viewBtn.setOnAction(e -> {
            DonorRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a donor row first.");
                return;
            }
            try {
                Donor d = ctx.getDonorController().findById(row.id()).orElse(null);
                if (d == null) {
                    FxUtil.showError(stage, "Donor not found.");
                    return;
                }
                long days = EligibilityCalculator.daysUntilNextEligible(d, LocalDate.now());
                String eligibility = days == 0 ? "Eligible by date (subject to Hb at donation)."
                        : "Wait " + days + " more day(s) before next whole-blood donation.";
                FxUtil.showInfo(stage, """
                        ID: %d
                        Name: %s
                        Phone: %s
                        Blood type: %s
                        DOB: %s
                        Last donation: %s
                        Active: %s
                        %s""".formatted(d.getId(), d.getFullName(), d.getPhone(), d.getBloodType(),
                        d.getDateOfBirth(), d.getLastDonationDate() == null ? "—" : d.getLastDonationDate(),
                        d.isActive() ? "Yes" : "No", eligibility));
            } catch (Exception ex) {
                FxUtil.showError(stage, ex.getMessage());
            }
        });
        addBtn.setOnAction(e -> {
            DonorForm form = DonorForm.prompt(stage, null);
            if (form != null) {
                FxUtil.runAsync(stage, () -> {
                    ctx.getDonorController().create(form.fullName(), form.phone(), form.email(),
                            form.bloodType(), form.dob());
                    return null;
                }, ignored -> reload.run());
            }
        });
        updateBtn.setOnAction(e -> {
            DonorRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a donor row first.");
                return;
            }
            try {
                Donor existing = ctx.getDonorController().findById(row.id()).orElse(null);
                if (existing == null) {
                    FxUtil.showError(stage, "Donor not found.");
                    return;
                }
                DonorForm form = DonorForm.prompt(stage, existing);
                if (form != null) {
                    FxUtil.runAsync(stage, () -> {
                        ctx.getDonorController().update(row.id(), form.fullName(), form.phone(), form.email(),
                                form.bloodType(), form.dob());
                        return null;
                    }, ignored -> reload.run());
                }
            } catch (Exception ex) {
                FxUtil.showError(stage, ex.getMessage());
            }
        });
        deactivateBtn.setOnAction(e -> {
            DonorRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select a donor row first.");
                return;
            }
            if (FxUtil.confirm(stage, "Soft-delete (deactivate) donor #" + row.id() + "?")) {
                FxUtil.runAsync(stage, () -> {
                    ctx.getDonorController().deactivate(row.id());
                    return null;
                }, ignored -> reload.run());
            }
        });

        root.setTop(toolbar);
        root.setCenter(table);
        reload.run();
        return root;
    }

    public record DonorRow(int id, String fullName, String phone, String bloodType, String active) {
        static DonorRow from(Donor d) {
            return new DonorRow(d.getId(), d.getFullName(), d.getPhone(), d.getBloodType(),
                    d.isActive() ? "Yes" : "No");
        }
    }

    private record DonorForm(String fullName, String phone, String email, String bloodType, LocalDate dob) {
        static DonorForm prompt(Stage stage, Donor existing) {
            TextField name = new TextField(existing != null ? existing.getFullName() : "");
            TextField phone = new TextField(existing != null ? existing.getPhone() : "");
            TextField email = new TextField(existing != null && existing.getEmail() != null ? existing.getEmail() : "");
            ComboBox<String> blood = new ComboBox<>(FXCollections.observableArrayList(FxUtil.BLOOD_TYPES));
            if (existing != null) {
                blood.getSelectionModel().select(existing.getBloodType());
            } else {
                blood.getSelectionModel().selectFirst();
            }
            TextField dob = new TextField(existing != null ? existing.getDateOfBirth().toString() : "2000-01-01");
            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(8);
            grid.addRow(0, new Label("Full name"), name);
            grid.addRow(1, new Label("Phone"), phone);
            grid.addRow(2, new Label("Email"), email);
            grid.addRow(3, new Label("Blood type"), blood);
            grid.addRow(4, new Label("DOB (YYYY-MM-DD)"), dob);
            javafx.scene.control.ButtonType ok = javafx.scene.control.ButtonType.OK;
            javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog =
                    new javafx.scene.control.Dialog<>();
            dialog.setTitle(existing == null ? "Add donor" : "Update donor");
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ok, javafx.scene.control.ButtonType.CANCEL);
            dialog.initOwner(stage);
            if (dialog.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) != ok) {
                return null;
            }
            try {
                return new DonorForm(name.getText().trim(), phone.getText().trim(), email.getText().trim(),
                        blood.getValue(), LocalDate.parse(dob.getText().trim()));
            } catch (DateTimeParseException ex) {
                FxUtil.showError(stage, "Invalid date of birth.");
                return null;
            }
        }
    }
}
