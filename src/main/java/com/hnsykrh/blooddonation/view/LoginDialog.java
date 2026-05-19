package com.hnsykrh.blooddonation.view;

import com.hnsykrh.blooddonation.controller.AuthController;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.service.ServiceException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

public final class LoginDialog extends JDialog {

    private final AuthController authController;
    private SessionContext session;

    public LoginDialog(java.awt.Frame owner, DatabaseManager databaseManager) {
        super(owner, "Staff Login — Blood Donation System", true);
        this.authController = new AuthController(databaseManager);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(380, 220);
        setLocationRelativeTo(owner);

        JTextField usernameField = new JTextField(16);
        JPasswordField passwordField = new JPasswordField(16);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(new EmptyBorder(12, 12, 0, 12));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttons = new JPanel();
        buttons.add(loginButton);
        buttons.add(cancelButton);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        root.add(new JLabel("<html><b>BAXU 3113</b> Blood Donation Management</html>"), BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        loginButton.addActionListener(e -> attemptLogin(usernameField, passwordField));
        cancelButton.addActionListener(e -> dispose());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                session = null;
            }
        });
        getRootPane().setDefaultButton(loginButton);
        usernameField.requestFocusInWindow();
    }

    private void attemptLogin(JTextField usernameField, JPasswordField passwordField) {
        try {
            Optional<SessionContext> result = authController.login(
                    usernameField.getText(),
                    new String(passwordField.getPassword()));
            if (result.isPresent()) {
                session = result.get();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login", JOptionPane.ERROR_MESSAGE);
        }
    }

    public SessionContext getSession() {
        return session;
    }
}
