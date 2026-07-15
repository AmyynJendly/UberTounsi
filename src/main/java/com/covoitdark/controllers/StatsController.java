package com.covoitdark.controllers;

import com.covoitdark.dao.StatsDAO;
import com.covoitdark.models.Stats;
import com.covoitdark.utils.SessionManager;

public class StatsController {

    private final StatsDAO statsDAO = new StatsDAO();

    public Stats getMesStats() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        Stats stats = statsDAO.findByUser(userId);
        if (stats == null) {
            statsDAO.create(userId);
            stats = statsDAO.findByUser(userId);
        }
        return stats;
    }

    public double getNbArbresSauves() {
        Stats stats = getMesStats();
        return stats != null ? stats.getTreesEquivalent() : 0.0;
    }

    /** Feature 10: total money earned as driver (sum of all captured payments). */
    public double getDriverEarnings() {
        Stats stats = getMesStats();
        return stats != null ? stats.getMoneyEarned() : 0.0;
    }

    /** Feature 6: current cancellation rate (0.0–1.0) for the logged-in user. */
    public double getCancellationRate() {
        Stats stats = getMesStats();
        return stats != null ? stats.getCancellationRate() : 0.0;
    }
}
