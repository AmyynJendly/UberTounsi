package com.covoitdark.controllers;

import com.covoitdark.dao.StatsDAO;
import com.covoitdark.dao.UserDAO;
import com.covoitdark.models.User;
import com.covoitdark.models.Passager;
import com.covoitdark.models.Chauffeur;
import com.covoitdark.models.Admin;
import com.covoitdark.utils.PasswordUtils;
import com.covoitdark.utils.SessionManager;

import java.util.List;

public class AuthController {

    private final UserDAO userDAO = new UserDAO();
    private final StatsDAO statsDAO = new StatsDAO();

    public boolean creerCompte(String fullName, String email, String password, String phone, User.Role role) {
        if (userDAO.emailExists(email)) {
            System.err.println("Email déjà utilisé.");
            return false;
        }

        String validationError = PasswordUtils.validate(password);
        if (validationError != null) {
            System.err.println(validationError);
            return false;
        }

        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hash(password, salt);

        // ADMIN role cannot be self-assigned via public registration
        User user;
        if (role == User.Role.DRIVER) {
            user = new Chauffeur(0, fullName, email, hash, salt, phone, "", null, 0.0, false, false, 0.0);
        } else {
            user = new Passager(0, fullName, email, hash, salt, phone, "", null, 0.0, false, false, 0.0);
        }
        if (userDAO.create(user)) {
            statsDAO.create(user.getId()); // Initialize stats
            return true;
        }
        return false;
    }

    public boolean authentifier(String identifier, String password) {
        User user = userDAO.findByEmailOrPhone(identifier);
        if (user == null) {
            System.err.println("Utilisateur introuvable.");
            return false;
        }

        if (user.isBlocked()) {
            System.err.println("Ce compte a été bloqué par l'administrateur.");
            return false;
        }

        if (PasswordUtils.verify(password, user.getPassword(), user.getSalt())) {
            userDAO.resetFailedLogins(user.getId());
            SessionManager.getInstance().setCurrentUser(user);
            return true;
        } else {
            userDAO.incrementFailedLogins(user.getId());
            User updatedUser = userDAO.findByIdDirect(user.getId());
            if (updatedUser != null && updatedUser.getFailedLogins() >= PasswordUtils.getMaxFailedAttempts()) {
                userDAO.setBlocked(user.getId(), true);
                System.err.println("Trop de tentatives échouées. Compte bloqué.");
            } else {
                System.err.println("Mot de passe incorrect.");
            }
            return false;
        }
    }

    public void deconnecter() {
        SessionManager.getInstance().logout();
    }

    public boolean modifierCompte(User user) {
        if (SessionManager.getInstance().getCurrentUserId() != user.getId()) return false;
        if (userDAO.update(user)) {
            SessionManager.getInstance().setCurrentUser(userDAO.findByIdDirect(user.getId()));
            return true;
        }
        return false;
    }

    // Admin methods
    public boolean bloquerUtilisateur(int userId) {
        if (!SessionManager.getInstance().isAdmin()) return false;
        return userDAO.setBlocked(userId, true);
    }

    public boolean suspendreCompte(int userId) {
        if (!SessionManager.getInstance().isAdmin()) return false;
        return userDAO.setBlocked(userId, true);
    }

    public void checkAdminDefault() {
        if (!userDAO.emailExists("admin@covoitdark.tn")) {
            String salt = PasswordUtils.generateSalt();
            String hash = PasswordUtils.hash("Admin1234!", salt);
            User admin = new User(0, "Administrateur", "admin@covoitdark.tn", hash, salt, "", User.Role.ADMIN, "", null, 0, true, false, 0.0);
            userDAO.create(admin);
        }
    }

    public List<User> tousLesUtilisateurs() {
        if (!SessionManager.getInstance().isAdmin()) return null;
        return userDAO.findAll();
    }
}
