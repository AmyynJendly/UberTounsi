package com.covoitdark.controllers;

import com.covoitdark.dao.TripChatDAO;
import com.covoitdark.dao.RequestDAO;
import com.covoitdark.dao.TripDAO;
import com.covoitdark.models.TripChatMessage;
import com.covoitdark.models.Request;
import com.covoitdark.models.Trip;
import com.covoitdark.utils.SessionManager;

import java.util.List;

/**
 * Feature 3 – Trip group chat.
 * Only the driver and accepted passengers of a trip may read or post.
 */
public class TripChatController {

    private final TripChatDAO chatDAO = new TripChatDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    public String sendMessage(int tripId, String content) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == -1) return "Non connecté.";
        if (content == null || content.isBlank()) return "Message vide.";

        if (!canParticipate(tripId, userId)) return "Accès refusé : vous n'êtes pas participant de ce trajet.";

        TripChatMessage msg = new TripChatMessage(tripId, userId, content.trim());
        return chatDAO.create(msg) ? "Message envoyé." : "Erreur lors de l'envoi.";
    }

    public List<TripChatMessage> getMessages(int tripId) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (!canParticipate(tripId, userId)) return List.of();
        return chatDAO.findByTrip(tripId);
    }

    /** Returns true if userId is the driver OR an accepted passenger of tripId. */
    private boolean canParticipate(int tripId, int userId) {
        Trip trip = tripDAO.findByIdDirect(tripId);
        if (trip == null) return false;
        if (trip.getDriverId() == userId) return true;
        return requestDAO.findByTrip(tripId).stream()
                .anyMatch(r -> r.getPassengerId() == userId && r.getStatus() == Request.Status.ACCEPTED);
    }
}
