package com.hnsykrh.blooddonation.controller;

import com.hnsykrh.blooddonation.dao.StaffDao;
import com.hnsykrh.blooddonation.db.DatabaseManager;
import com.hnsykrh.blooddonation.model.Staff;
import com.hnsykrh.blooddonation.service.ServiceException;
import com.hnsykrh.blooddonation.view.SessionContext;

import java.sql.SQLException;
import java.util.Optional;

public final class AuthController {

    private final DatabaseManager databaseManager;
    private final StaffDao staffDao = new StaffDao();

    public AuthController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<SessionContext> login(String username, String password) throws ServiceException {
        if (username == null || username.isBlank()) {
            throw new ServiceException("Username is required.");
        }
        if (password == null || password.isBlank()) {
            throw new ServiceException("Password is required.");
        }
        try (var connection = databaseManager.openConnection()) {
            Optional<Staff> staff = staffDao.findActiveByCredentials(connection, username.trim(), password);
            return staff.map(s -> new SessionContext(s.getId(), s.getUsername(), s.getFullName(), s.getRole()));
        } catch (SQLException e) {
            throw new ServiceException("Login failed: " + e.getMessage(), e);
        }
    }
}
