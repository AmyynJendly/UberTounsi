package com.covoitdark.models;

import java.time.LocalDateTime;

/** Feature 7 – A passenger's saved/favorite driver. */
public class DriverFavorite {

    private int id;
    private int passengerId;
    private int driverId;
    private String driverName;   // joined
    private LocalDateTime createdAt;

    public DriverFavorite() {}

    public DriverFavorite(int passengerId, int driverId) {
        this.passengerId = passengerId;
        this.driverId = driverId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
