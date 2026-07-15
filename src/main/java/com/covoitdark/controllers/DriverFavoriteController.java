package com.covoitdark.controllers;

import com.covoitdark.dao.DriverFavoriteDAO;
import com.covoitdark.dao.NotificationDAO;
import com.covoitdark.dao.TripDAO;
import com.covoitdark.models.DriverFavorite;
import com.covoitdark.models.Notification;
import com.covoitdark.models.Trip;
import com.covoitdark.utils.SessionManager;

import java.util.List;

/**
 * Feature 7 – Saved/Favorite Drivers.
 *
 * Passengers follow drivers they rate highly. When a followed driver posts a
 * new trip, all followers are notified automatically.
 */
public class DriverFavoriteController {

    private final DriverFavoriteDAO favoriteDAO = new DriverFavoriteDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final TripDAO tripDAO = new TripDAO();

    public boolean addFavorite(int driverId) {
        int passengerId = SessionManager.getInstance().getCurrentUserId();
        if (passengerId == -1) return false;
        return favoriteDAO.add(passengerId, driverId);
    }

    public boolean removeFavorite(int driverId) {
        int passengerId = SessionManager.getInstance().getCurrentUserId();
        if (passengerId == -1) return false;
        return favoriteDAO.remove(passengerId, driverId);
    }

    public boolean isFavorite(int driverId) {
        int passengerId = SessionManager.getInstance().getCurrentUserId();
        if (passengerId == -1) return false;
        return favoriteDAO.isFavorite(passengerId, driverId);
    }

    public List<DriverFavorite> getMyFavorites() {
        int passengerId = SessionManager.getInstance().getCurrentUserId();
        if (passengerId == -1) return List.of();
        return favoriteDAO.findByPassenger(passengerId);
    }

    /**
     * Call this whenever a driver publishes a new trip.
     * Notifies all passengers who have saved that driver as a favorite.
     * Stream: maps passenger ids to Notification objects.
     */
    public void notifyFollowersOfNewTrip(Trip trip) {
        List<Integer> followers = favoriteDAO.findPassengersByDriver(trip.getDriverId());
        followers.stream()
                .map(passengerId -> {
                    Notification n = new Notification();
                    n.setUserId(passengerId);
                    n.setTitle("Nouveau trajet de votre chauffeur favori");
                    n.setMessage("Un trajet " + trip.getDeparture() + " → " + trip.getArrival()
                            + " le " + trip.getStartDate() + " à " + trip.getDepartureTime()
                            + " vient d'être publié.");
                    return n;
                })
                .forEach(notificationDAO::create);
    }
}
