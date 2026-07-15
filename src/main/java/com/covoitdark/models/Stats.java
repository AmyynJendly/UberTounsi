package com.covoitdark.models;

import java.time.LocalDateTime;

public class Stats {
    private int id;
    private int userId;
    private int totalTrips;
    private double moneySaved;
    private double co2Saved;
    private double moneyEarned;    // Feature 10: driver earnings
    private int cancelledTrips;    // Feature 6: cancellation rate
    private LocalDateTime updatedAt;

    public Stats() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTotalTrips() { return totalTrips; }
    public void setTotalTrips(int totalTrips) { this.totalTrips = totalTrips; }

    public double getMoneySaved() { return moneySaved; }
    public void setMoneySaved(double moneySaved) { this.moneySaved = moneySaved; }

    public double getCo2Saved() { return co2Saved; }
    public void setCo2Saved(double co2Saved) { this.co2Saved = co2Saved; }

    public double getTreesEquivalent() {
        return co2Saved / 25.0;
    }

    public double getMoneyEarned() { return moneyEarned; }
    public void setMoneyEarned(double moneyEarned) { this.moneyEarned = moneyEarned; }

    public int getCancelledTrips() { return cancelledTrips; }
    public void setCancelledTrips(int cancelledTrips) { this.cancelledTrips = cancelledTrips; }

    /** Cancellation rate as a fraction 0.0–1.0 */
    public double getCancellationRate() {
        int total = totalTrips + cancelledTrips;
        return total == 0 ? 0.0 : (double) cancelledTrips / total;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
