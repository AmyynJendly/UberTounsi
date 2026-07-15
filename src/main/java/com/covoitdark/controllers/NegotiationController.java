package com.covoitdark.controllers;

import com.covoitdark.dao.RequestDAO;
import com.covoitdark.dao.TripDAO;
import com.covoitdark.dao.NotificationDAO;
import com.covoitdark.models.Request;
import com.covoitdark.models.Trip;
import com.covoitdark.models.Notification;
import com.covoitdark.utils.SessionManager;

/**
 * Feature 8 – Price Negotiation.
 *
 * Flow:
 *  1. Passenger submits a counter-offer price after creating a PENDING booking.
 *  2. Request status moves to NEGOTIATING; driver is notified.
 *  3. Driver accepts the counter-offer (new price becomes the trip price for that booking)
 *     or rejects it (request stays PENDING at original price, or driver rejects outright).
 *
 * Demonstrates Open/Closed Principle: negotiation behaviour is added here without
 * modifying RequestController or TripController.
 */
public class NegotiationController {

    private final RequestDAO requestDAO = new RequestDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final PaymentController paymentController = new PaymentController();

    /** Passenger proposes a counter price. */
    public String proposeCounterOffer(int requestId, double counterPrice) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        Request req = requestDAO.findById(requestId);
        if (req == null || req.getPassengerId() != userId) return "Non autorisé.";
        if (req.getStatus() != Request.Status.PENDING) return "La négociation n'est possible que sur une demande en attente.";
        if (counterPrice <= 0) return "Prix invalide.";

        Trip trip = tripDAO.findByIdDirect(req.getTripId());
        if (trip == null) return "Trajet introuvable.";
        if (counterPrice >= trip.getPrice()) return "Votre offre doit être inférieure au prix affiché.";

        requestDAO.updateCounterPrice(requestId, counterPrice);

        Notification n = new Notification();
        n.setUserId(trip.getDriverId());
        n.setTitle("Offre de prix reçue");
        n.setMessage("Un passager propose " + String.format("%.2f", counterPrice)
                + " TND pour votre trajet " + trip.getDeparture() + " → " + trip.getArrival()
                + " (prix affiché : " + String.format("%.2f", trip.getPrice()) + " TND).");
        notificationDAO.create(n);

        return "Contre-offre envoyée au chauffeur.";
    }

    /** Driver accepts the counter-offer: captures payment at negotiated price. */
    public String acceptCounterOffer(int requestId) {
        int driverId = SessionManager.getInstance().getCurrentUserId();
        Request req = requestDAO.findById(requestId);
        if (req == null) return "Demande introuvable.";
        Trip trip = tripDAO.findByIdDirect(req.getTripId());
        if (trip == null || trip.getDriverId() != driverId) return "Non autorisé.";
        if (req.getStatus() != Request.Status.NEGOTIATING) return "Aucune contre-offre en attente.";

        double agreedPrice = req.getCounterPrice();
        if (!paymentController.capturerPaiement(req.getPassengerId(), driverId, agreedPrice)) {
            return "Échec du paiement (solde insuffisant ?).";
        }

        tripDAO.decrementSeats(trip.getId());
        requestDAO.updateStatus(requestId, Request.Status.ACCEPTED);

        Notification n = new Notification();
        n.setUserId(req.getPassengerId());
        n.setTitle("Contre-offre acceptée !");
        n.setMessage("Le chauffeur a accepté votre offre de " + String.format("%.2f", agreedPrice)
                + " TND. Votre réservation est confirmée.");
        notificationDAO.create(n);

        return "Contre-offre acceptée. Réservation confirmée à " + String.format("%.2f", agreedPrice) + " TND.";
    }

    /** Driver rejects the counter-offer: request falls back to PENDING at original price. */
    public String rejectCounterOffer(int requestId) {
        int driverId = SessionManager.getInstance().getCurrentUserId();
        Request req = requestDAO.findById(requestId);
        if (req == null) return "Demande introuvable.";
        Trip trip = tripDAO.findByIdDirect(req.getTripId());
        if (trip == null || trip.getDriverId() != driverId) return "Non autorisé.";

        requestDAO.updateStatus(requestId, Request.Status.PENDING);

        Notification n = new Notification();
        n.setUserId(req.getPassengerId());
        n.setTitle("Contre-offre refusée");
        n.setMessage("Le chauffeur n'a pas accepté votre offre. Votre demande reste au prix original de "
                + String.format("%.2f", trip.getPrice()) + " TND.");
        notificationDAO.create(n);

        return "Contre-offre refusée. La demande est revenue au statut PENDING.";
    }
}
