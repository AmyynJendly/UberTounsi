package com.covoitdark.controllers;

import com.covoitdark.dao.NotificationDAO;
import com.covoitdark.models.Notification;
import com.covoitdark.utils.SessionManager;

import java.util.List;

public class NotificationController {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    public List<Notification> getMesNotifications() {
        return notificationDAO.findByUser(SessionManager.getInstance().getCurrentUserId());
    }

    public void markAsRead(int notificationId) {
        notificationDAO.markRead(notificationId);
    }

    public void markAllAsRead() {
        notificationDAO.markAllRead(SessionManager.getInstance().getCurrentUserId());
    }
    
    public int getUnreadCount() {
        return notificationDAO.countUnread(SessionManager.getInstance().getCurrentUserId());
    }

    // Teacher required simulated external notifications
    public boolean notifierSMS(int userId, String message) {
        // System.out.println("Envoi SMS à l'utilisateur " + userId + " : " + message);
        sendInAppNotification(userId, "Nouveau SMS", message);
        return true;
    }

    public boolean notifierEmail(int userId, String subject, String body) {
        // System.out.println("Envoi Email à " + userId + " - Sujet: " + subject);
        sendInAppNotification(userId, subject, body);
        return true;
    }

    private void sendInAppNotification(int userId, String title, String message) {
        Notification notif = new Notification(0, userId, title, message, false);
        notificationDAO.create(notif);
    }
}
