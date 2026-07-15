package com.covoitdark.models;

import java.sql.Timestamp;

public class MoyenPaiement {
    public enum Type { WALLET, CARD, CASH }

    private int id;
    private int userId;
    private String type;
    private String lastFour;
    private String brand;
    private String expiry;
    private Timestamp createdAt;

    public MoyenPaiement() {}

    // New constructor for the modern backend
    public MoyenPaiement(int id, int userId, String type, String lastFour, String brand, String expiry) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.lastFour = lastFour;
        this.brand = brand;
        this.expiry = expiry;
    }

    // Compatibility constructor for PaymentController
    public MoyenPaiement(int id, int userId, Type type, String brand, String lastFour, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.type = type.name();
        this.brand = brand;
        this.lastFour = lastFour != null ? lastFour : "****";
        this.expiry = "N/A";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLastFour() { return lastFour; }
    public void setLastFour(String lastFour) { this.lastFour = lastFour; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
