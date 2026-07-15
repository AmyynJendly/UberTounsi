package com.covoitdark.controllers;

import com.covoitdark.dao.RatingDAO;
import com.covoitdark.dao.UserDAO;
import com.covoitdark.dao.RequestDAO;
import com.covoitdark.dao.TripDAO;
import com.covoitdark.models.Rating;
import com.covoitdark.models.Request;
import com.covoitdark.models.Trip;
import com.covoitdark.utils.SessionManager;

import java.util.List;

public class RatingController {

    private final RatingDAO ratingDAO = new RatingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final TripDAO tripDAO = new TripDAO();

    public boolean evaluerChauffeur(int tripId, int driverId, int score, String comment) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == -1) return false;
        if (score < 1 || score > 5) return false;

        // Verify the trip is complete
        Trip trip = tripDAO.findByIdDirect(tripId);
        if (trip == null || trip.getStatus() != Trip.Status.COMPLETE) return false;

        // Verify the rater was actually an accepted passenger on this trip
        boolean wasPassenger = requestDAO.findByTrip(tripId).stream()
                .anyMatch(r -> r.getPassengerId() == userId && r.getStatus() == Request.Status.ACCEPTED);
        if (!wasPassenger) return false;

        Rating rating = new Rating(0, tripId, userId, driverId, score, comment);
        if (ratingDAO.create(rating)) {
            updateUserReputation(driverId);
            return true;
        }
        return false;
    }

    public List<Rating> consulterNotesChauffeur(int driverId) {
        return ratingDAO.findByRatedUser(driverId);
    }

    private void updateUserReputation(int userId) {
        List<Rating> ratings = ratingDAO.findByRatedUser(userId);
        if (ratings.isEmpty()) return;

        double average = ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(5.0);

        userDAO.updateReputationScore(userId, average);
    }
}
