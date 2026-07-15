package com.covoitdark.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Badge {

    public enum BadgeType {
        FIRST_TRIP("🚀 Premier Trajet", "Vous avez effectué votre premier trajet !"),
        ECO_WARRIOR("🌱 Éco-Guerrier", "500 kg de CO₂ économisés"),
        ROAD_KING("👑 Road King", "50 trajets en tant que conducteur"),
        TRIP_MASTER("🏆 Trip Master", "100 trajets complétés"),
        TOP_RATED("⭐ Top Noté", "Note moyenne ≥ 4.8"),
        SUPER_DRIVER("🚗 Super Conducteur", "20 trajets avec note 5 étoiles"),
        FREQUENT_TRAVELER("✈️ Grand Voyageur", "10 trajets en tant que passager"),
        MONEY_SAVER("💰 Économiste", "200 TND économisés"),
        SOCIAL_BUTTERFLY("🦋 Social", "10 conversations engagées");

        private final String label;
        private final String description;

        BadgeType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    private int id;
    private int userId;
    private String badgeName;
    private LocalDateTime awardedAt;

    public Badge() {}

    public Badge(int id, int userId, String badgeName) {
        this.id = id;
        this.userId = userId;
        this.badgeName = badgeName;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getBadgeName() { return badgeName; }
    public LocalDateTime getAwardedAt() { return awardedAt; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
    public void setAwardedAt(LocalDateTime awardedAt) { this.awardedAt = awardedAt; }

    public String getLabel() {
        for (BadgeType bt : BadgeType.values()) {
            if (bt.name().equals(badgeName)) return bt.getLabel();
        }
        return badgeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Badge b)) return false;
        return userId == b.userId && Objects.equals(badgeName, b.badgeName);
    }

    @Override
    public int hashCode() { return Objects.hash(userId, badgeName); }

    @Override
    public String toString() {
        return "Badge{userId=" + userId + ", badge='" + badgeName + "'}";
    }
}
