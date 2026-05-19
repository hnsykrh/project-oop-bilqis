package com.hnsykrh.blooddonation.view;

import com.hnsykrh.blooddonation.controller.InventoryController;
import com.hnsykrh.blooddonation.controller.ReportController;
import com.hnsykrh.blooddonation.model.BloodInventory;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

final class InventoryTabPanel extends JPanel implements SwingUi.ComponentSource {

    private final InventoryController inventoryController;
    private final ReportController reportController;
    private final DefaultTableModel tableModel;
    private final JTable table;

    InventoryTabPanel(InventoryController inventoryController, ReportController reportController) {
        super(new BorderLayout());
        this.inventoryController = inventoryController;
        this.reportController = reportController;
        tableModel = new DefaultTableModel(new String[]{"ID", "Blood type", "Stock (mL)", "Updated at"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JButton refreshBtn = new JButton("Refresh");
        JButton viewBtn = new JButton("View");
        JButton pdfBtn = new JButton("Export PDF");
        JPanel top = SwingUi.toolbar(refreshBtn, viewBtn, pdfBtn);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refreshBtn.addActionListener(e -> reload());
        viewBtn.addActionListener(e -> viewSelected());
        pdfBtn.addActionListener(e -> exportPdf());
        reload();
    }

    private void reload() {
        SwingUi.runAsync(this, null, () -> inventoryController.listAll(), this::fillTable);
    }

    private void fillTable(List<BloodInventory> rows) {
        tableModel.setRowCount(0);
        for (BloodInventory row : rows) {
            tableModel.addRow(new Object[]{
                    row.getId(),
                    row.getBloodType(),
                    row.getStockMl(),
                    row.getUpdatedAt()
            });
        }
    }

    private void viewSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            SwingUi.showError(this, "Select an inventory row first.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        SwingUi.showInfo(this, """
                Blood type: %s
                Stock: %s mL
                Last updated: %s""".formatted(
                tableModel.getValueAt(row, 1),
                tableModel.getValueAt(row, 2),
                tableModel.getValueAt(row, 3)));
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("inventory-" + LocalDate.now() + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        SwingUi.runAsync(this, null, () -> {
            reportController.exportInventoryPdf(path);
            return path;
        }, saved -> SwingUi.showInfo(this, "PDF saved to:\n" + saved));
    }

    @Override
    public java.awt.Component component() {
        return this;
    }
}
