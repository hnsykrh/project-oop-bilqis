package com.hnsykrh.blooddonation.view;

import com.hnsykrh.blooddonation.controller.RequestController;
import com.hnsykrh.blooddonation.model.RecipientRequest;
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

final class RequestsTabPanel extends JPanel implements SwingUi.ComponentSource {

    private final RequestController controller;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);
    private final JCheckBox openOnlyBox = new JCheckBox("Open only", false);

    RequestsTabPanel(RequestController controller) {
        super(new BorderLayout());
        this.controller = controller;
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Patient", "Hospital", "Blood", "Needed mL", "Fulfilled", "Remaining", "Date", "Cancelled"},
                0) {
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
        JButton fulfillBtn = new JButton("Fulfill");
        JButton cancelBtn = new JButton("Cancel");

        JPanel top = SwingUi.toolbar(new JLabel("Search:"), searchField, openOnlyBox, searchBtn, refreshBtn,
                viewBtn, addBtn, updateBtn, fulfillBtn, cancelBtn);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> reload());
        refreshBtn.addActionListener(e -> reload());
        viewBtn.addActionListener(e -> viewSelected());
        addBtn.addActionListener(e -> addRequest());
        updateBtn.addActionListener(e -> updateRequest());
        fulfillBtn.addActionListener(e -> fulfillSelected());
        cancelBtn.addActionListener(e -> cancelSelected());
        reload();
    }

    private void reload() {
        SwingUi.runAsync(this, null,
                () -> controller.search(SwingUi.text(searchField), openOnlyBox.isSelected()),
                this::fillTable);
    }

    private void fillTable(List<RecipientRequest> requests) {
        tableModel.setRowCount(0);
        for (RecipientRequest r : requests) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getPatientReference(),
                    r.getHospitalName(),
                    r.getBloodType(),
                    r.getUnitsNeededMl(),
                    r.getFulfilledMl(),
                    r.remainingMl(),
                    r.getRequestDate(),
                    r.isCancelled() ? "Yes" : "No"
            });
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            SwingUi.showError(this, "Select a request row first.");
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
            RecipientRequest r = controller.findById(id).orElse(null);
            if (r == null) {
                SwingUi.showError(this, "Request not found.");
                return;
            }
            SwingUi.showInfo(this, """
                    ID: %d
                    Patient: %s
                    Hospital: %s
                    Blood type: %s
                    Needed: %d mL
                    Fulfilled: %d mL
                    Remaining: %d mL
                    Request date: %s
                    Cancelled: %s
                    Notes: %s""".formatted(
                    r.getId(), r.getPatientReference(), r.getHospitalName(), r.getBloodType(),
                    r.getUnitsNeededMl(), r.getFulfilledMl(), r.remainingMl(), r.getRequestDate(),
                    r.isCancelled() ? "Yes" : "No",
                    r.getNotes() == null ? "—" : r.getNotes()));
        } catch (Exception ex) {
            SwingUi.showError(this, ex.getMessage());
        }
    }

    private void addRequest() {
        RequestForm form = RequestForm.prompt(this, null);
        if (form == null) {
            return;
        }
        SwingUi.runAsync(this, null, () -> {
            controller.create(form.patient(), form.hospital(), form.bloodType(), form.units(), form.date(), form.notes());
            return null;
        }, ignored -> reload());
    }

    private void updateRequest() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        try {
            RecipientRequest existing = controller.findById(id).orElse(null);
            if (existing == null) {
                SwingUi.showError(this, "Request not found.");
                return;
            }
            RequestForm form = RequestForm.prompt(this, existing);
            if (form == null) {
                return;
            }
            SwingUi.runAsync(this, null, () -> {
                controller.update(id, form.patient(), form.hospital(), form.bloodType(), form.units(), form.date(),
                        form.notes());
                return null;
            }, ignored -> reload());
        } catch (Exception ex) {
            SwingUi.showError(this, ex.getMessage());
        }
    }

    private void fulfillSelected() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        JTextField amountField = new JTextField("250", 8);
        JPanel panel = SwingUi.formPanel();
        SwingUi.addFormRow(panel, 0, "Amount to fulfill (mL)", amountField);
        int result = javax.swing.JOptionPane.showConfirmDialog(this, panel, "Fulfill request #" + id,
                javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        try {
            int requested = Integer.parseInt(SwingUi.text(amountField));
            SwingUi.runAsync(this, null, () -> controller.fulfill(id, requested),
                    actual -> {
                        SwingUi.showInfo(this, "Fulfilled " + actual + " mL.");
                        reload();
                    });
        } catch (NumberFormatException ex) {
            SwingUi.showError(this, "Invalid amount.");
        }
    }

    private void cancelSelected() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Cancel (soft-delete) request #" + id + "?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        SwingUi.runAsync(this, null, () -> {
            controller.cancel(id);
            return null;
        }, ignored -> reload());
    }

    @Override
    public java.awt.Component component() {
        return this;
    }

    private record RequestForm(String patient, String hospital, String bloodType, int units, LocalDate date,
                               String notes) {
        static RequestForm prompt(java.awt.Component parent, RecipientRequest existing) {
            JTextField patient = new JTextField(existing != null ? existing.getPatientReference() : "", 20);
            JTextField hospital = new JTextField(existing != null ? existing.getHospitalName() : "", 20);
            JComboBox<String> blood = new JComboBox<>(SwingUi.BLOOD_TYPES);
            if (existing != null) {
                blood.setSelectedItem(existing.getBloodType());
            }
            JTextField units = new JTextField(existing != null ? String.valueOf(existing.getUnitsNeededMl()) : "500",
                    8);
            JTextField date = new JTextField(existing != null ? existing.getRequestDate().toString()
                    : LocalDate.now().toString(), 12);
            JTextField notes = new JTextField(existing != null && existing.getNotes() != null ? existing.getNotes() : "",
                    24);
            JPanel panel = SwingUi.formPanel();
            SwingUi.addFormRow(panel, 0, "Patient reference", patient);
            SwingUi.addFormRow(panel, 1, "Hospital", hospital);
            SwingUi.addFormRow(panel, 2, "Blood type", blood);
            SwingUi.addFormRow(panel, 3, "Units needed (mL)", units);
            SwingUi.addFormRow(panel, 4, "Request date", date);
            SwingUi.addFormRow(panel, 5, "Notes", notes);
            int result = javax.swing.JOptionPane.showConfirmDialog(parent, panel,
                    existing == null ? "Add request" : "Update request", javax.swing.JOptionPane.OK_CANCEL_OPTION);
            if (result != javax.swing.JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                return new RequestForm(SwingUi.text(patient), SwingUi.text(hospital), (String) blood.getSelectedItem(),
                        Integer.parseInt(SwingUi.text(units)), LocalDate.parse(SwingUi.text(date)), SwingUi.text(notes));
            } catch (DateTimeParseException | NumberFormatException ex) {
                javax.swing.JOptionPane.showMessageDialog(parent, "Invalid date or units.", "Validation",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
    }
}
