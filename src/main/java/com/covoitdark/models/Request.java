package com.covoitdark.models;

import java.time.LocalDateTime;

public class Request {
    public enum Status { PENDING, ACCEPTED, REJECTED, CANCELLED, WAITLIST, COMPLETE, NEGOTIATING }

    private int id;
    private int tripId;
    private int passengerId;
    private int quantity;
    private double totalCost;
    private Status status;
    private LocalDateTime createdAt;
    private String passengerName; // Joined field
    private String secretCode;
    private double counterPrice;    // Feature 8: passenger counter-offer price
    private String luggageSize;     // Feature 9: passenger declared luggage

    public Request() {
        this.status = Status.PENDING;
    }

    public Request(int id, int tripId, int passengerId, Status status) {
        this.id = id;
        this.tripId = tripId;
        this.passengerId = passengerId;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public boolean isAccepted() { return status == Status.ACCEPTED; }
    public boolean isRejected() { return status == Status.REJECTED; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getSecretCode() { return secretCode; }
    public void setSecretCode(String secretCode) { this.secretCode = secretCode; }

    public double getCounterPrice() { return counterPrice; }
    public void setCounterPrice(double counterPrice) { this.counterPrice = counterPrice; }

    public String getLuggageSize() { return luggageSize; }
    public void setLuggageSize(String luggageSize) { this.luggageSize = luggageSize; }
}
