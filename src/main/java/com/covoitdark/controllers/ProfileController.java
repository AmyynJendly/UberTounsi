package com.covoitdark.controllers;

import com.covoitdark.dao.CarDAO;
import com.covoitdark.dao.RatingDAO;
import com.covoitdark.dao.BadgeDAO;
import com.covoitdark.models.Car;
import com.covoitdark.models.Rating;
import com.covoitdark.models.Badge;
import com.covoitdark.utils.SessionManager;

import java.util.List;

public class ProfileController {

    private final CarDAO carDAO = new CarDAO();
    private final RatingDAO ratingDAO = new RatingDAO();
    private final BadgeDAO badgeDAO = new BadgeDAO();

    // -- Vehicules --

    public List<Car> getVehicules() {
        return carDAO.findByDriver(SessionManager.getInstance().getCurrentUserId());
    }

    public String ajouterVehicule(Car car) {
        if (!SessionManager.getInstance().isDriver()) return "Seuls les conducteurs peuvent ajouter des véhicules.";
        
        if (carDAO.licensePlateExists(car.getLicensePlate())) {
            return "Cette plaque d'immatriculation existe déjà.";
        }
        
        car.setDriverId(SessionManager.getInstance().getCurrentUserId());
        return carDAO.create(car) ? "Véhicule ajouté." : "Erreur lors de l'ajout.";
    }

    public boolean supprimerVehicule(int carId) {
        Car car = carDAO.findById(carId);
        if (car != null && car.getDriverId() == SessionManager.getInstance().getCurrentUserId()) {
            return carDAO.delete(carId);
        }
        return false;
    }

    // -- Notes & Avis --

    public String evaluerUtilisateur(int tripId, int ratedId, int score, String comment) {
        int raterId = SessionManager.getInstance().getCurrentUserId();
        if (raterId == ratedId) return "Vous ne pouvez pas vous évaluer vous-même.";
        
        if (ratingDAO.hasRated(raterId, tripId, ratedId)) {
            return "Vous avez déjà évalué cet utilisateur pour ce trajet.";
        }
        
        Rating rating = new Rating(0, tripId, raterId, ratedId, score, comment);
        return ratingDAO.create(rating) ? "Avis enregistré." : "Erreur lors de l'enregistrement de l'avis.";
    }

    public List<Rating> consulterNotes(int userId) {
        return ratingDAO.findByRatedUser(userId);
    }
    
    public double getMoyenneNotes(int userId) {
        return ratingDAO.getAverageScore(userId);
    }

    // -- Badges --

    public List<Badge> getMesBadges() {
        return badgeDAO.findByUser(SessionManager.getInstance().getCurrentUserId());
    }
}
