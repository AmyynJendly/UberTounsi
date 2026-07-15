package com.covoitdark.controllers;

import com.covoitdark.dao.TripDAO;
import com.covoitdark.dao.RequestDAO;
import com.covoitdark.dao.NotificationDAO;
import com.covoitdark.dao.StatsDAO;
import com.covoitdark.dao.UserDAO;
import com.covoitdark.models.Trip;
import com.covoitdark.models.Request;
import com.covoitdark.models.Notification;
import com.covoitdark.models.Stats;
import com.covoitdark.models.User;
import com.covoitdark.utils.SessionManager;
import com.covoitdark.utils.BadgeManager;
import com.covoitdark.utils.CO2Calculator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class TripController {

    private final TripDAO tripDAO = new TripDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final StatsDAO statsDAO = new StatsDAO();
    private final BadgeManager badgeManager = new BadgeManager();
    private final PaymentController paymentController = new PaymentController();
    private final DriverFavoriteController favoriteController = new DriverFavoriteController();

    public boolean proposerTrajet(Trip trip) {
        if (!SessionManager.getInstance().isDriver()) return false;
        trip.setDriverId(SessionManager.getInstance().getCurrentUserId());
        boolean created = tripDAO.create(trip);
        if (created) favoriteController.notifyFollowersOfNewTrip(trip); // Feature 7
        return created;
    }

    public List<Trip> getTrajets() {
        return tripDAO.findByDriver(SessionManager.getInstance().getCurrentUserId());
    }

    public List<Trip> searchTrips(String departure, String arrival, LocalDate date) {
        return tripDAO.search(departure, arrival, date);
    }

    public List<Trip> getActiveTrips() {
        return tripDAO.findAllActive();
    }

    public boolean cloreTrajet(int tripId) {
        Trip trip = tripDAO.findByIdDirect(tripId);
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (trip == null || trip.getDriverId() != currentUserId) return false;

        boolean closed = tripDAO.updateStatus(tripId, Trip.Status.COMPLETE);
        if (closed) {
            // Compute accepted list once — used multiple times below
            List<Request> accepted = requestDAO.findByTrip(tripId).stream()
                    .filter(r -> r.getStatus() == Request.Status.ACCEPTED)
                    .collect(Collectors.toList());

            statsDAO.incrementTrips(currentUserId, accepted.size());
            double co2 = CO2Calculator.calculateCO2Saved(trip.getDeparture(), trip.getArrival(), accepted.size());
            statsDAO.addCO2(currentUserId, co2);

            User driver = userDAO.findByIdDirect(currentUserId);
            double avgRating = driver != null ? driver.getReputationScore() : 5.0;
            // Single lookup — avoid double-call NPE between null-check and .getTotalTrips()
            Stats driverStats = statsDAO.findByUser(currentUserId);
            int tripsAsDriver = driverStats != null ? driverStats.getTotalTrips() : 0;
            badgeManager.checkAndAwardBadges(currentUserId, avgRating, tripsAsDriver, 0);

            // CO2 per passenger: divide by total occupants (driver + passengers)
            double co2PerPassenger = co2 / Math.max(1, accepted.size() + 1);

            // Notify passengers that trip is complete
            for (Request r : accepted) {
                Notification n = new Notification();
                n.setUserId(r.getPassengerId());
                n.setTitle("Trajet terminé");
                n.setMessage("Votre trajet " + trip.getDeparture() + " → " + trip.getArrival() + " est terminé. N'oubliez pas d'évaluer votre chauffeur !");
                notificationDAO.create(n);
                statsDAO.incrementTrips(r.getPassengerId(), 1);
                statsDAO.addCO2(r.getPassengerId(), co2PerPassenger);
                // Single lookup per passenger to avoid NPE between null-check and value access
                Stats passengerStats = statsDAO.findByUser(r.getPassengerId());
                int passengerTrips = passengerStats != null ? passengerStats.getTotalTrips() : 1;
                badgeManager.checkAndAwardBadges(r.getPassengerId(), 5.0, 0, passengerTrips);
            }
        }
        return closed;
    }

    public String annulerTrajet(int tripId) {
        Trip trip = tripDAO.findByIdDirect(tripId);
        if (trip == null || trip.getDriverId() != SessionManager.getInstance().getCurrentUserId()) {
            return "Non autorisé.";
        }

        List<Request> acceptedRequests = requestDAO.findByTrip(tripId).stream()
                .filter(r -> r.getStatus() == Request.Status.ACCEPTED)
                .collect(java.util.stream.Collectors.toList());

        User driver = userDAO.findByIdDirect(trip.getDriverId());

        int passengersCount = acceptedRequests.size();
        LocalDateTime departureDateTime = trip.getStartDate().atTime(trip.getDepartureTime());
        boolean isLateCancellation = LocalDateTime.now().isAfter(departureDateTime.minusHours(24));
        double penaltyPerPassenger = isLateCancellation ? trip.getPrice() * 0.20 : 0.0;

        // Refund all accepted passengers and notify them
        for (Request r : acceptedRequests) {
            paymentController.rembourser(r.getPassengerId(), r.getTotalCost());

            // Deduct penalty from driver if late cancellation
            if (isLateCancellation && driver != null) {
                User freshDriver = userDAO.findByIdDirect(driver.getId());
                if (freshDriver != null) {
                    userDAO.updateBalance(driver.getId(), Math.max(0, freshDriver.getBalance() - penaltyPerPassenger));
                }
            }

            // Notify passenger
            Notification n = new Notification();
            n.setUserId(r.getPassengerId());
            n.setTitle("Trajet annulé");
            n.setMessage("Le trajet " + trip.getDeparture() + " → " + trip.getArrival() + " a été annulé par le chauffeur. Vous avez été remboursé intégralement.");
            notificationDAO.create(n);

            requestDAO.updateStatus(r.getId(), Request.Status.CANCELLED);
        }

        tripDAO.updateStatus(tripId, Trip.Status.CANCELLED);

        if (passengersCount > 0) {
            if (isLateCancellation) {
                double totalPenalty = penaltyPerPassenger * passengersCount;
                return "Trajet annulé. " + passengersCount + " passager(s) remboursé(s). Pénalité d'annulation tardive déduite : " + String.format("%.2f", totalPenalty) + " TND.";
            } else {
                return "Trajet annulé. " + passengersCount + " passager(s) remboursé(s) intégralement.";
            }
        } else {
            return "Trajet annulé avec succès.";
        }
    }
}
