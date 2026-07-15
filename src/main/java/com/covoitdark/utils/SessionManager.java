package com.covoitdark.utils;

import com.covoitdark.models.User;

/**
 * Singleton session manager — holds the currently logged-in user.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public User getCurrentUser() { return currentUser; }

    public void setCurrentUser(User user) { this.currentUser = user; }

    public boolean isLoggedIn() { return currentUser != null; }

    public boolean isDriver() {
        return isLoggedIn() && currentUser.isDriver();
    }

    public boolean isPassenger() {
        return isLoggedIn() && currentUser.isPassenger();
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }

    public int getCurrentUserId() {
        return isLoggedIn() ? currentUser.getId() : -1;
    }

    public void logout() { this.currentUser = null; }

    @Override
    public String toString() {
        return "SessionManager{user=" + (currentUser != null ? currentUser.getEmail() : "null") + "}";
    }
}
