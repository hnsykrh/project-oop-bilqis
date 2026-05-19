package com.hnsykrh.blooddonation.view;

import com.hnsykrh.blooddonation.controller.DonationController;
import com.hnsykrh.blooddonation.controller.DonorController;
import com.hnsykrh.blooddonation.model.Donation;
import com.hnsykrh.blooddonation.model.Donor;
import com.hnsykrh.blooddonation.service.EligibilityCalculator;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

final class DonationsTabPanel extends JPanel implements SwingUi.ComponentSource {

    private final DonationController donationController;
    private final DonorController donorController;
    private final SessionContext session;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);
    private final JCheckBox nonVoidedOnly = new JCheckBox("Non-voided only", false);

    DonationsTabPanel(DonationController donationController, DonorController donorController, SessionContext session) {
        super(new BorderLayout());
        this.donationController = donationController;
        this.donorController = donorController;
        this.session = session;
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Donor", "Blood", "Date", "Volume mL", "Hb g/dL", "Voided"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        JButton searchBtn = new JButton("Search");
        JButton refreshBtn = new JButton("Refresh");
        JButton viewBtn = new JButton("View");
        JButton recordBtn = new JButton("Record donation");
        JButton voidBtn = new JButton("Void");

        JPanel top = SwingUi.toolbar(new JLabel("Search:"), searchField, nonVoidedOnly, searchBtn, refreshBtn,
                viewBtn, recordBtn, voidBtn);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> reload());
        refreshBtn.addActionListener(e -> reload());
        viewBtn.addActionListener(e -> viewSelected());
        recordBtn.addActionListener(e -> recordDonation());
        voidBtn.addActionListener(e -> voidSelected());
        reload();
    }

    private void reload() {
        SwingUi.runAsync(this, null,
                () -> donationController.search(SwingUi.text(searchField), nonVoidedOnly.isSelected()),
                this::fillTable);
    }

    private void fillTable(List<Donation> donations) {
        tableModel.setRowCount(0);
        for (Donation d : donations) {
            tableModel.addRow(new Object[]{
                    d.getId(),
                    d.getDonorName(),
                    d.getDonorBloodType(),
                    d.getDonationDate(),
                    d.getVolumeMl(),
                    d.getHemoglobinGdl(),
                    d.isVoided() ? "Yes" : "No"
            });
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            SwingUi.showError(this, "Select a donation row first.");
            return null;
        }
        return (Integer) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private void viewSelected() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        try {
            Donation d = donationController.findById(id).orElse(null);
            if (d == null) {
                SwingUi.showError(this, "Donation not found.");
                return;
            }
            SwingUi.showInfo(this, """
                    ID: %d
                    Donor: %s (#%d)
                    Blood type: %s
                    Date: %s
                    Volume: %d mL
                    Hemoglobin: %.1f g/dL
                    Staff ID: %s
                    Voided: %s
                    Void reason: %s""".formatted(
                    d.getId(), d.getDonorName(), d.getDonorId(), d.getDonorBloodType(),
                    d.getDonationDate(), d.getVolumeMl(), d.getHemoglobinGdl(),
                    d.getStaffId() == null ? "—" : d.getStaffId(),
                    d.isVoided() ? "Yes" : "No",
                    d.getVoidReason() == null ? "—" : d.getVoidReason()));
        } catch (Exception ex) {
            SwingUi.showError(this, ex.getMessage());
        }
    }

    private void recordDonation() {
        JTextField donorIdField = new JTextField(8);
        JTextField dateField = new JTextField(LocalDate.now().toString(), 12);
        JTextField volumeField = new JTextField("450", 8);
        JTextField hbField = new JTextField("13.5", 8);
        JPanel panel = SwingUi.formPanel();
        SwingUi.addFormRow(panel, 0, "Donor ID", donorIdField);
        SwingUi.addFormRow(panel, 1, "Donation date (YYYY-MM-DD)", dateField);
        SwingUi.addFormRow(panel, 2, "Volume (mL)", volumeField);
        SwingUi.addFormRow(panel, 3, "Hemoglobin (g/dL)", hbField);
        int result = javax.swing.JOptionPane.showConfirmDialog(this, panel, "Record donation",
                javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        try {
            int donorId = Integer.parseInt(SwingUi.text(donorIdField));
            LocalDate date = LocalDate.parse(SwingUi.text(dateField));
            int volume = Integer.parseInt(SwingUi.text(volumeField));
            double hb = Double.parseDouble(SwingUi.text(hbField));
            Donor donor = donorController.findById(donorId).orElse(null);
            if (donor == null) {
                SwingUi.showError(this, "Donor not found.");
                return;
            }
            var check = EligibilityCalculator.evaluate(donor, date, hb);
            if (!check.eligible()) {
                SwingUi.showError(this, check.message());
                return;
            }
            SwingUi.runAsync(this, null, () -> donationController.recordDonation(
                    donorId, date, volume, hb, session.getStaffId()), ignored -> reload());
        } catch (NumberFormatException | DateTimeParseException ex) {
            SwingUi.showError(this, "Invalid numeric or date input.");
        } catch (Exception ex) {
            SwingUi.showError(this, ex.getMessage());
        }
    }

    private void voidSelected() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        JTextField reason = new JTextField(24);
        JPanel panel = SwingUi.formPanel();
        SwingUi.addFormRow(panel, 0, "Void reason", reason);
        int result = javax.swing.JOptionPane.showConfirmDialog(this, panel, "Void donation #" + id,
                javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        SwingUi.runAsync(this, null, () -> {
            donationController.voidDonation(id, SwingUi.text(reason));
            return null;
        }, ignored -> reload());
    }

    @Override
    public java.awt.Component component() {
        return this;
    }
}
