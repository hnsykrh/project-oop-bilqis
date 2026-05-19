package com.hnsykrh.blooddonation.view;

/**
 * Logged-in staff session passed from login to {@link MainFrame}.
 */
public final class SessionContext {

    private final int staffId;
    private final String username;
    private final String fullName;
    private final String role;

    public SessionContext(int staffId, String username, String fullName, String role) {
        this.staffId = staffId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
