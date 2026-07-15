package com.covoitdark.utils;

import com.covoitdark.dao.BadgeDAO;
import com.covoitdark.dao.StatsDAO;
import com.covoitdark.models.Badge;
import com.covoitdark.models.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates and awards badges using a pluggable list of BadgeRule implementations.
 *
 * Demonstrates:
 *  - SOLID / Open-Closed Principle: new badges are added by implementing BadgeRule
 *    and registering it here — BadgeManager itself never needs modification.
 *  - Collections framework: rules stored in an ArrayList<BadgeRule>
 */
public class BadgeManager {

    private final BadgeDAO badgeDAO = new BadgeDAO();
    private final StatsDAO statsDAO = new StatsDAO();

    /** All active badge rules. To add a new badge: implement BadgeRule and add it here. */
    private final List<BadgeRule> rules = new ArrayList<>(List.of(
        new FirstTripRule(),
        new EcoWarriorRule(),
        new RoadKingRule(),
        new TripMasterRule(),
        new TopRatedRule(),
        new SuperDriverRule(),
        new FrequentTravelerRule(),
        new MoneySaverRule()
    ));

    /**
     * Check and award all applicable badges for the given user.
     * Call this after every trip completion or rating event.
     */
    public void checkAndAwardBadges(int userId, double avgRating, int tripsAsDriver, int tripsAsPassenger) {
        Stats stats = statsDAO.findByUser(userId);
        if (stats == null) return;

        for (BadgeRule rule : rules) {
            if (rule.isEligible(stats, avgRating, tripsAsDriver, tripsAsPassenger)) {
                awardIfEligible(userId, rule.getBadgeType());
            }
        }
    }

    private void awardIfEligible(int userId, Badge.BadgeType type) {
        if (badgeDAO.hasBadge(userId, type.name())) return;
        badgeDAO.create(new Badge(0, userId, type.name()));
    }

    // ── Built-in rules (one class per badge = OCP) ───────────────────────────

    private static class FirstTripRule implements BadgeRule {
        @Override public Badge.BadgeType getBadgeType() { return Badge.BadgeType.FIRST_TRIP; }
        @Override public boolean isEligible(Stats s, double r, int d, int p) { return s.getTotalTrips() >= 1; }
    }

    private static class EcoWarriorRule implements BadgeRule {
        @Override public Badge.BadgeType getBadgeType() { return Badge.BadgeType.ECO_WARRIOR; }
        @Override public boolean isEligible(Stats s, double r, int d, int p) { return s.getCo2Saved() >= 500; }
    }

    private static class RoadKingRule implements BadgeRule {
        @Override public Badge.BadgeType getBadgeType() { return Badge.BadgeType.ROAD_KING; }
        @Override public boolean isEligible(Stats s, double r, int d, int p) { return d >= 50; }
    }

    private static class TripMasterRule implements BadgeRule {
        @Override public Badge.BadgeType getBadgeType() { return Badge.BadgeType.TRIP_MASTER; }
        @Override public boolean isEligible(Stats s, double r, int d, int p) { return s.getTotalTrips() >= 100; }
    }

    private static class TopRatedRule implements BadgeRule {
        @Override public Badge.BadgeType getBadgeType() { return Badge.BadgeType.TOP_RATED; }
        @Override public boolean isEligible(Stats s, double r, int d, int p) { return r >= 4.8 && s.getTotalTrips() >= 5; }
    }

    private static class SuperDriverRule implements BadgeRule {
        @Override public Badge.BadgeType getBadgeType() { return Badge.BadgeType.SUPER_DRIVER; }
        @Override public boolean isEligible(Stats s, double r, int d, int p) { return d >= 20 && r >= 5.0; }
    }

    private static class FrequentTravelerRule implements BadgeRule {
        @Override public Badge.BadgeType getBadgeType() { return Badge.BadgeType.FREQUENT_TRAVELER; }
        @Override public boolean isEligible(Stats s, double r, int d, int p) { return p >= 10; }
    }

    private static class MoneySaverRule implements BadgeRule {
        @Override public Badge.BadgeType getBadgeType() { return Badge.BadgeType.MONEY_SAVER; }
        @Override public boolean isEligible(Stats s, double r, int d, int p) { return s.getMoneySaved() >= 200; }
    }
}
