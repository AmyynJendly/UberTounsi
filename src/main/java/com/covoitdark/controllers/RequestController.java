package com.covoitdark.controllers;

import com.covoitdark.dao.RequestDAO;
import com.covoitdark.dao.TripDAO;
import com.covoitdark.dao.StatsDAO;
import com.covoitdark.dao.UserDAO;
import com.covoitdark.models.Request;
import com.covoitdark.models.Trip;
import com.covoitdark.models.Stats;
import com.covoitdark.models.User;
import com.covoitdark.models.Notification;
import com.covoitdark.utils.SessionManager;
import com.covoitdark.utils.CO2Calculator;
import com.covoitdark.dao.NotificationDAO;
import com.covoitdark.dao.BlockDAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class RequestController {

    private final RequestDAO requestDAO = new RequestDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final StatsDAO statsDAO = new StatsDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final PaymentController paymentController = new PaymentController();

    /** Cancellation rate threshold above which a warning notification is sent. */
    private static final double CANCELLATION_WARN_THRESHOLD = 0.30;

    /**
     * Feature 9: overload that accepts passenger luggage size declaration.
     * Validates against the trip's luggage policy.
     */
    public String creerReservation(int tripId, String luggageSize) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == -1) return "Non connecté.";

        Trip trip = tripDAO.findByIdDirect(tripId);
        if (trip == null) return "Trajet introuvable.";
        if (trip.getDriverId() == userId) return "Vous ne pouvez pas réserver votre propre trajet.";
        if (!trip.isActive()) return "Trajet non disponible.";

        // Feature 11: driver blacklist check
        if (blockDAO.isBlocked(trip.getDriverId(), userId)) {
            return "Réservation impossible : le chauffeur ne vous accepte pas sur ce trajet.";
        }

        // Validate luggage: NONE policy means no luggage allowed
        if ("NONE".equalsIgnoreCase(trip.getLuggagePolicy().name()) &&
                luggageSize != null && !"NONE".equalsIgnoreCase(luggageSize)) {
            return "Ce trajet n'accepte pas de bagages.";
        }

        if (requestDAO.existsForPassengerAndTrip(userId, tripId)) {
            return "Vous avez déjà une demande pour ce trajet.";
        }
        if (!paymentController.payer(userId, trip.getPrice())) {
            return "Échec de l'autorisation de paiement.";
        }

        Request req = new Request(0, tripId, userId, trip.isFull() ? Request.Status.WAITLIST : Request.Status.PENDING);
        req.setSecretCode(generateSecretCode());
        req.setLuggageSize(luggageSize != null ? luggageSize : "NONE");
        if (requestDAO.create(req)) {
            return trip.isFull() ? "Placé sur liste d'attente." : "Demande de réservation envoyée.";
        }
        return "Erreur lors de la réservation.";
    }

    public String creerReservation(int tripId) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == -1) return "Non connecté.";

        Trip trip = tripDAO.findByIdDirect(tripId);
        if (trip == null) return "Trajet introuvable.";
        if (trip.getDriverId() == userId) return "Vous ne pouvez pas réserver votre propre trajet.";
        if (!trip.isActive()) return "Trajet non disponible.";

        // Feature 11: driver may have blacklisted this passenger
        if (blockDAO.isBlocked(trip.getDriverId(), userId)) {
            return "Réservation impossible : le chauffeur ne vous accepte pas sur ce trajet.";
        }

        if (requestDAO.existsForPassengerAndTrip(userId, tripId)) {
            return "Vous avez déjà une demande pour ce trajet.";
        }

        // Teacher required: Immediate authorization on booking
        if (!paymentController.payer(userId, trip.getPrice())) {
            return "Échec de l'autorisation de paiement.";
        }

        Request req = new Request(0, tripId, userId, trip.isFull() ? Request.Status.WAITLIST : Request.Status.PENDING);
        req.setSecretCode(generateSecretCode()); // Feature 2: boarding verification code
        if (requestDAO.create(req)) {
            return trip.isFull() ? "Placé sur liste d'attente." : "Demande de réservation envoyée.";
        }
        return "Erreur lors de la réservation.";
    }

    public List<Request> getMesReservations() {
        return requestDAO.findByPassenger(SessionManager.getInstance().getCurrentUserId());
    }

    public List<Request> getDemandesPourTrajet(int tripId) {
        return requestDAO.findByTrip(tripId);
    }

    public boolean confirmerReservation(int requestId) {
        Request req = requestDAO.findById(requestId);
        if (req == null) return false;
        
        Trip trip = tripDAO.findByIdDirect(req.getTripId());
        if (trip == null || trip.getDriverId() != SessionManager.getInstance().getCurrentUserId()) return false;
        
        if (trip.getAvailableSeats() > 0) {
            // Capture payment: deduct from passenger, credit driver
            if (!paymentController.capturerPaiement(req.getPassengerId(), trip.getDriverId(), req.getTotalCost())) {
                return false;
            }

            tripDAO.decrementSeats(trip.getId());
            return requestDAO.updateStatus(requestId, Request.Status.ACCEPTED);
        }
        return false;
    }

    public boolean rejeterReservation(int requestId) {
        Request req = requestDAO.findById(requestId);
        if (req == null) return false;
        
        Trip trip = tripDAO.findByIdDirect(req.getTripId());
        if (trip == null || trip.getDriverId() != SessionManager.getInstance().getCurrentUserId()) return false;
        
        // No payment was captured at PENDING stage, so no refund needed on rejection
        return requestDAO.updateStatus(requestId, Request.Status.REJECTED);
    }

    /** Feature 2: driver calls this at boarding to confirm a passenger actually boarded. */
    public boolean verifierCodeEmbarquement(int requestId, String code) {
        Request req = requestDAO.findById(requestId);
        if (req == null || !req.isAccepted()) return false;
        Trip trip = tripDAO.findByIdDirect(req.getTripId());
        if (trip == null || trip.getDriverId() != SessionManager.getInstance().getCurrentUserId()) return false;
        return code != null && code.equalsIgnoreCase(req.getSecretCode());
    }

    private String generateSecretCode() {
        int code = new Random().nextInt(900000) + 100000; // 6-digit code
        return String.valueOf(code);
    }

    public String annulerReservation(int requestId) {
        Request req = requestDAO.findById(requestId);
        if (req == null || req.getPassengerId() != SessionManager.getInstance().getCurrentUserId()) {
            return "Non autorisé.";
        }

        Trip trip = tripDAO.findByIdDirect(req.getTripId());
        if (req.isAccepted() && trip != null) {
            tripDAO.incrementSeats(trip.getId());

            LocalDateTime departureDateTime = trip.getStartDate().atTime(trip.getDepartureTime());
            if (LocalDateTime.now().isAfter(departureDateTime.minusHours(24))) {
                // Late cancellation: 50% refund
                double refund = req.getTotalCost() * 0.50;
                paymentController.rembourser(req.getPassengerId(), refund);
                // Deduct refund amount from driver balance (driver keeps the other 50%)
                User driver = userDAO.findByIdDirect(trip.getDriverId());
                if (driver != null) {
                    userDAO.updateBalance(driver.getId(), Math.max(0, driver.getBalance() - refund));
                }
                requestDAO.updateStatus(requestId, Request.Status.CANCELLED);
                return String.format("Réservation annulée. Remboursement partiel (50%%) : %.2f TND.", refund);
            } else {
                // Full refund
                paymentController.rembourser(req.getPassengerId(), req.getTotalCost());
                // Deduct full amount from driver
                User driver = userDAO.findByIdDirect(trip.getDriverId());
                if (driver != null) {
                    userDAO.updateBalance(driver.getId(), Math.max(0, driver.getBalance() - req.getTotalCost()));
                }
            }
        }

        requestDAO.updateStatus(requestId, Request.Status.CANCELLED);

        // Feature 6: track cancellation rate
        int cancellingUserId = SessionManager.getInstance().getCurrentUserId();
        statsDAO.incrementCancelled(cancellingUserId);
        updateCancellationRate(cancellingUserId);

        return "Réservation annulée." + (req.isAccepted() ? " Remboursement intégral effectué." : "");
    }

    private void updateCancellationRate(int userId) {
        Stats stats = statsDAO.findByUser(userId);
        if (stats == null) return;
        double rate = stats.getCancellationRate();
        userDAO.updateCancellationRate(userId, rate);
        if (rate >= CANCELLATION_WARN_THRESHOLD) {
            Notification warn = new Notification();
            warn.setUserId(userId);
            warn.setTitle("Attention : taux d'annulation élevé");
            warn.setMessage(String.format(
                "Votre taux d'annulation est de %.0f%%. Un taux supérieur à 30%% peut limiter vos réservations.", rate * 100));
            notificationDAO.create(warn);
        }
    }
}
