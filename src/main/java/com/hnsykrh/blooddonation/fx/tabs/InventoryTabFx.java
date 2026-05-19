package com.hnsykrh.blooddonation.fx.tabs;

import com.hnsykrh.blooddonation.fx.FxAppContext;
import com.hnsykrh.blooddonation.fx.FxUtil;
import com.hnsykrh.blooddonation.model.BloodInventory;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;

public final class InventoryTabFx {

    private InventoryTabFx() {
    }

    public static BorderPane build(FxAppContext ctx, Stage stage) {
        BorderPane root = new BorderPane();
        Button refreshBtn = new Button("Refresh");
        Button viewBtn = new Button("View");
        Button pdfBtn = new Button("Export PDF (OpenPDF)");
        HBox toolbar = new HBox(8, refreshBtn, viewBtn, pdfBtn);
        toolbar.setPadding(new Insets(8));

        TableView<InventoryRow> table = new TableView<>(FXCollections.observableArrayList());
        TableColumn<InventoryRow, String> c1 = new TableColumn<>("Blood type");
        c1.setCellValueFactory(new PropertyValueFactory<>("bloodType"));
        TableColumn<InventoryRow, String> c2 = new TableColumn<>("Stock (mL)");
        c2.setCellValueFactory(new PropertyValueFactory<>("stock"));
        TableColumn<InventoryRow, String> c3 = new TableColumn<>("Updated");
        c3.setCellValueFactory(new PropertyValueFactory<>("updated"));
        table.getColumns().addAll(c1, c2, c3);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Runnable reload = () -> FxUtil.runAsync(stage, ctx.getInventoryController()::listAll, rows ->
                table.getItems().setAll(rows.stream().map(InventoryRow::from).toList()));

        refreshBtn.setOnAction(e -> reload.run());
        viewBtn.setOnAction(e -> {
            InventoryRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                FxUtil.showError(stage, "Select an inventory row first.");
                return;
            }
            FxUtil.showInfo(stage, "Blood type: " + row.bloodType() + "\nStock: " + row.stock()
                    + " mL\nLast updated: " + row.updated());
        });
        pdfBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save inventory PDF");
            chooser.setInitialFileName("inventory-" + LocalDate.now() + ".pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            java.io.File file = chooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }
            Path path = file.toPath();
            FxUtil.runAsync(stage, () -> {
                ctx.getReportController().exportInventoryPdf(path);
                return path;
            }, saved -> FxUtil.showInfo(stage, "PDF saved to:\n" + saved));
        });

        root.setTop(toolbar);
        root.setCenter(table);
        reload.run();
        return root;
    }

    private record InventoryRow(String bloodType, String stock, String updated) {
        static InventoryRow from(BloodInventory row) {
            return new InventoryRow(row.getBloodType(), String.valueOf(row.getStockMl()), row.getUpdatedAt());
        }
    }
}
