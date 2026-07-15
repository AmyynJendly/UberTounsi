package com.covoitdark.controllers;

import com.covoitdark.dao.UserDAO;
import com.covoitdark.dao.NotificationDAO;
import com.covoitdark.models.User;
import com.covoitdark.models.Notification;
import com.covoitdark.utils.SessionManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Feature 5 – Driver ID document verification.
 *
 * Flow:
 *  1. Driver submits their ID document (stored as base64 in their avatar or a
 *     dedicated field — here we piggyback on an admin flag for simplicity).
 *  2. Admin calls approveIdVerification(driverId) or rejectIdVerification(driverId).
 *  3. Driver's id_verified flag is set; a notification is sent.
 *
 * Demonstrates SOLID / Single Responsibility: this controller owns only the
 * ID-verification lifecycle, separate from profile or auth concerns.
 */
public class IdVerificationController {

    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    /** Admin: list all drivers whose ID is NOT yet verified. */
    public List<User> getPendingVerifications() {
        if (!SessionManager.getInstance().isAdmin()) return List.of();
        return userDAO.findAll().stream()
                .filter(u -> u.isDriver() && !u.isIdVerified())
                .collect(Collectors.toList());
    }

    /** Admin: approve a driver's ID. */
    public boolean approveIdVerification(int driverId) {
        if (!SessionManager.getInstance().isAdmin()) return false;
        boolean ok = userDAO.setIdVerified(driverId, true);
        if (ok) notify(driverId, "Identité vérifiée ✔", "Votre pièce d'identité a été approuvée. Vous pouvez maintenant publier des trajets.");
        return ok;
    }

    /** Admin: reject a driver's ID submission. */
    public boolean rejectIdVerification(int driverId) {
        if (!SessionManager.getInstance().isAdmin()) return false;
        boolean ok = userDAO.setIdVerified(driverId, false);
        if (ok) notify(driverId, "Vérification refusée", "Votre pièce d'identité n'a pas été acceptée. Veuillez soumettre un document valide.");
        return ok;
    }

    private void notify(int userId, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        notificationDAO.create(n);
    }
}
