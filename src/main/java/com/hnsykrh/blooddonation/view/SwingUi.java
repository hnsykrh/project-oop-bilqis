package com.hnsykrh.blooddonation.view;

import com.hnsykrh.blooddonation.service.ServiceException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

final class SwingUi {

    static final String[] BLOOD_TYPES = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    private SwingUi() {
    }

    static void showError(ComponentSource source, String message) {
        JOptionPane.showMessageDialog(source.component(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void showInfo(ComponentSource source, String message) {
        JOptionPane.showMessageDialog(source.component(), message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    @FunctionalInterface
    interface AsyncTask<T> {
        T execute() throws Exception;
    }

    static <T> void runAsync(ComponentSource source, Runnable onStart, AsyncTask<T> task, Consumer<T> onSuccess) {
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.execute();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    onSuccess.accept(result);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = cause instanceof ServiceException se ? se.getMessage() : cause.getMessage();
                    showError(source, msg != null ? msg : "Operation failed.");
                }
            }
        };
        if (onStart != null) {
            onStart.run();
        }
        worker.execute();
    }

    static JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    static void addFormRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints labelGc = new GridBagConstraints();
        labelGc.gridx = 0;
        labelGc.gridy = row;
        labelGc.anchor = GridBagConstraints.WEST;
        labelGc.insets = new Insets(4, 4, 4, 8);
        form.add(new JLabel(label), labelGc);
        GridBagConstraints fieldGc = new GridBagConstraints();
        fieldGc.gridx = 1;
        fieldGc.gridy = row;
        fieldGc.fill = GridBagConstraints.HORIZONTAL;
        fieldGc.weightx = 1;
        fieldGc.insets = new Insets(4, 0, 4, 4);
        form.add(field, fieldGc);
    }

    static JPanel toolbar(JComponent... components) {
        JPanel bar = new JPanel();
        for (JComponent c : components) {
            bar.add(c);
        }
        return bar;
    }

    static String text(JTextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    interface ComponentSource {
        java.awt.Component component();
    }

    static class BorderPanel extends JPanel implements ComponentSource {
        BorderPanel(JComponent center) {
            super(new BorderLayout());
            add(center, BorderLayout.CENTER);
        }

        @Override
        public java.awt.Component component() {
            return this;
        }
    }
}
