package com.hnsykrh.blooddonation.view;

import com.hnsykrh.blooddonation.controller.DonorController;
import com.hnsykrh.blooddonation.model.Donor;
import com.hnsykrh.blooddonation.service.EligibilityCalculator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

final class DonorsTabPanel extends JPanel implements SwingUi.ComponentSource {

    private final DonorController controller;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);
    private final JCheckBox activeOnlyBox = new JCheckBox("Active only", true);

    DonorsTabPanel(DonorController controller) {
        super(new BorderLayout());
        this.controller = controller;
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Phone", "Email", "Blood", "DOB", "Last donation", "Active"}, 0) {
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
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deactivateBtn = new JButton("Deactivate");

        JPanel top = SwingUi.toolbar(new JLabel("Search:"), searchField, activeOnlyBox, searchBtn, refreshBtn,
                viewBtn, addBtn, updateBtn, deactivateBtn);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> reload());
        refreshBtn.addActionListener(e -> reload());
        viewBtn.addActionListener(e -> viewSelected());
        addBtn.addActionListener(e -> addDonor());
        updateBtn.addActionListener(e -> updateDonor());
        deactivateBtn.addActionListener(e -> deactivateSelected());
        reload();
    }

    private void reload() {
        SwingUi.runAsync(this, null,
                () -> controller.search(SwingUi.text(searchField), activeOnlyBox.isSelected()),
                this::fillTable);
    }

    private void fillTable(List<Donor> donors) {
        tableModel.setRowCount(0);
        for (Donor d : donors) {
            tableModel.addRow(new Object[]{
                    d.getId(),
                    d.getFullName(),
                    d.getPhone(),
                    d.getEmail() == null ? "" : d.getEmail(),
                    d.getBloodType(),
                    d.getDateOfBirth(),
                    d.getLastDonationDate() == null ? "" : d.getLastDonationDate(),
                    d.isActive() ? "Yes" : "No"
            });
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            SwingUi.showError(this, "Select a donor row first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        return (Integer) tableModel.getValueAt(modelRow, 0);
    }

    private void viewSelected() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        try {
            Donor d = controller.findById(id).orElse(null);
            if (d == null) {
                SwingUi.showError(this, "Donor not found.");
                return;
            }
            long days = EligibilityCalculator.daysUntilNextEligible(d, LocalDate.now());
            String eligibility = days == 0 ? "Eligible by date (subject to Hb at donation)."
                    : "Wait " + days + " more day(s) before next whole-blood donation.";
            SwingUi.showInfo(this, """
                    ID: %d
                    Name: %s
                    Phone: %s
                    Email: %s
                    Blood type: %s
                    DOB: %s
                    Last donation: %s
                    Active: %s
                    %s""".formatted(
                    d.getId(), d.getFullName(), d.getPhone(),
                    d.getEmail() == null ? "—" : d.getEmail(),
                    d.getBloodType(), d.getDateOfBirth(),
                    d.getLastDonationDate() == null ? "—" : d.getLastDonationDate(),
                    d.isActive() ? "Yes" : "No",
                    eligibility));
        } catch (Exception ex) {
            SwingUi.showError(this, ex.getMessage());
        }
    }

    private void addDonor() {
        DonorForm form = DonorForm.prompt(this, null);
        if (form == null) {
            return;
        }
        SwingUi.runAsync(this, null, () -> {
            controller.create(form.fullName(), form.phone(), form.email(), form.bloodType(), form.dob());
            return null;
        }, ignored -> reload());
    }

    private void updateDonor() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        try {
            Donor existing = controller.findById(id).orElse(null);
            if (existing == null) {
                SwingUi.showError(this, "Donor not found.");
                return;
            }
            DonorForm form = DonorForm.prompt(this, existing);
            if (form == null) {
                return;
            }
            SwingUi.runAsync(this, null, () -> {
                controller.update(id, form.fullName(), form.phone(), form.email(), form.bloodType(), form.dob());
                return null;
            }, ignored -> reload());
        } catch (Exception ex) {
            SwingUi.showError(this, ex.getMessage());
        }
    }

    private void deactivateSelected() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Soft-delete (deactivate) donor #" + id + "?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        SwingUi.runAsync(this, null, () -> {
            controller.deactivate(id);
            return null;
        }, ignored -> reload());
    }

    @Override
    public java.awt.Component component() {
        return this;
    }

    private record DonorForm(String fullName, String phone, String email, String bloodType, LocalDate dob) {
        static DonorForm prompt(java.awt.Component parent, Donor existing) {
            JTextField name = new JTextField(existing != null ? existing.getFullName() : "", 20);
            JTextField phone = new JTextField(existing != null ? existing.getPhone() : "", 20);
            JTextField email = new JTextField(existing != null && existing.getEmail() != null
                    ? existing.getEmail() : "", 20);
            JComboBox<String> blood = new JComboBox<>(SwingUi.BLOOD_TYPES);
            if (existing != null) {
                blood.setSelectedItem(existing.getBloodType());
            }
            JTextField dob = new JTextField(existing != null ? existing.getDateOfBirth().toString() : "2000-01-01", 12);
            JPanel panel = SwingUi.formPanel();
            SwingUi.addFormRow(panel, 0, "Full name", name);
            SwingUi.addFormRow(panel, 1, "Phone", phone);
            SwingUi.addFormRow(panel, 2, "Email", email);
            SwingUi.addFormRow(panel, 3, "Blood type", blood);
            SwingUi.addFormRow(panel, 4, "Date of birth (YYYY-MM-DD)", dob);
            int result = javax.swing.JOptionPane.showConfirmDialog(parent, panel,
                    existing == null ? "Add donor" : "Update donor", javax.swing.JOptionPane.OK_CANCEL_OPTION);
            if (result != javax.swing.JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                return new DonorForm(SwingUi.text(name), SwingUi.text(phone), SwingUi.text(email),
                        (String) blood.getSelectedItem(), LocalDate.parse(SwingUi.text(dob)));
            } catch (DateTimeParseException ex) {
                javax.swing.JOptionPane.showMessageDialog(parent, "Invalid date of birth.", "Validation",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
    }
}
