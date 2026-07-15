package com.covoitdark.utils;

import com.covoitdark.models.Badge;
import com.covoitdark.models.Stats;

/**
 * Open/Closed Principle — BadgeRule is the extension point for badge logic.
 *
 * To add a new badge, create a class that implements BadgeRule and register it
 * in BadgeManager. BadgeManager itself never needs to be modified.
 *
 * Demonstrates:
 *  - SOLID / Open-Closed Principle: open for extension (new BadgeRule impl), closed for modification
 *  - Java Generics: works with any BadgeType via the generic contract
 */
public interface BadgeRule {
    /** The badge type this rule awards. */
    Badge.BadgeType getBadgeType();

    /** Return true when the user qualifies for this badge. */
    boolean isEligible(Stats stats, double avgRating, int tripsAsDriver, int tripsAsPassenger);
}
