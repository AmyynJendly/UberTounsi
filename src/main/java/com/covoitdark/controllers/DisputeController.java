package com.covoitdark.controllers;

import com.covoitdark.dao.DisputeDAO;
import com.covoitdark.dao.TripDAO;
import com.covoitdark.dao.RequestDAO;
import com.covoitdark.dao.NotificationDAO;
import com.covoitdark.dao.UserDAO;
import com.covoitdark.models.Dispute;
import com.covoitdark.models.Notification;
import com.covoitdark.models.Request;
import com.covoitdark.models.Trip;
import com.covoitdark.models.User;
import com.covoitdark.utils.SessionManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Feature 13 – Dispute Resolution.
 *
 * Passengers (or drivers) file a dispute after a trip completes.
 * Admin reviews and can:
 *  - RESOLVE: issue a manual refund adjustment
 *  - DISMISS: close without action
 *
 * Demonstrates:
 *  - Stream: filter open disputes by trip
 *  - SOLID / Single Responsibility: dispute lifecycle owned here only
 */
public class DisputeController {

    private final DisputeDAO disputeDAO = new DisputeDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PaymentController paymentController = new PaymentController();

    /** Any trip participant can file a dispute for a completed trip. */
    public String fileDispute(int tripId, String reason) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == -1) return "Non connecté.";
        if (reason == null || reason.isBlank()) return "Motif requis.";

        Trip trip = tripDAO.findByIdDirect(tripId);
        if (trip == null) return "Trajet introuvable.";

        // Only driver or accepted passengers may file
        boolean isDriver = trip.getDriverId() == userId;
        boolean wasPassenger = requestDAO.findByTrip(tripId).stream()
                .anyMatch(r -> r.getPassengerId() == userId && r.getStatus() == Request.Status.ACCEPTED);
        if (!isDriver && !wasPassenger) return "Vous n'étiez pas participant de ce trajet.";

        Dispute d = new Dispute();
        d.setTripId(tripId);
        d.setComplainantId(userId);
        d.setReason(reason);
        return disputeDAO.create(d) ? "Réclamation enregistrée. L'équipe la traitera sous 48h." : "Erreur lors de l'envoi.";
    }

    /** Admin: list all open disputes. */
    public List<Dispute> getOpenDisputes() {
        if (!SessionManager.getInstance().isAdmin()) return List.of();
        return disputeDAO.findOpen();
    }

    /** Admin: resolve a dispute with an optional manual refund. */
    public String resolve(int disputeId, String adminNote, double refundAmount) {
        if (!SessionManager.getInstance().isAdmin()) return "Accès refusé.";
        Dispute dispute = disputeDAO.findById(disputeId);
        if (dispute == null) return "Réclamation introuvable.";

        if (refundAmount > 0) {
            paymentController.rembourser(dispute.getComplainantId(), refundAmount);
        }

        disputeDAO.resolve(disputeId, adminNote, Dispute.Status.RESOLVED);
        notify(dispute.getComplainantId(), "Réclamation résolue",
                "Votre réclamation a été traitée. " + (refundAmount > 0
                        ? String.format("Un remboursement de %.2f TND a été effectué.", refundAmount)
                        : adminNote));
        return "Réclamation résolue.";
    }

    /** Admin: dismiss a dispute without action. */
    public String dismiss(int disputeId, String adminNote) {
        if (!SessionManager.getInstance().isAdmin()) return "Accès refusé.";
        Dispute dispute = disputeDAO.findById(disputeId);
        if (dispute == null) return "Réclamation introuvable.";

        disputeDAO.resolve(disputeId, adminNote, Dispute.Status.DISMISSED);
        notify(dispute.getComplainantId(), "Réclamation clôturée",
                "Votre réclamation a été examinée et clôturée. Note : " + adminNote);
        return "Réclamation clôturée.";
    }

    private void notify(int userId, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        notificationDAO.create(n);
    }
}
