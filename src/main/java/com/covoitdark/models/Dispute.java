package com.covoitdark.models;

import java.time.LocalDateTime;

/** Feature 13 – A dispute filed by a trip participant after payment issues. */
public class Dispute {

    public enum Status { OPEN, RESOLVED, DISMISSED }

    private int id;
    private int tripId;
    private int complainantId;
    private String complainantName; // joined
    private String reason;
    private Status status;
    private String adminNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;

    public Dispute() { this.status = Status.OPEN; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getComplainantId() { return complainantId; }
    public void setComplainantId(int complainantId) { this.complainantId = complainantId; }

    public String getComplainantName() { return complainantName; }
    public void setComplainantName(String complainantName) { this.complainantName = complainantName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
