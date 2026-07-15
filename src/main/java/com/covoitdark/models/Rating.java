package com.covoitdark.models;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int tripId;
    private int raterId;
    private int ratedId;
    private int score;
    private String comment;
    private LocalDateTime createdAt;
    private String raterName; // Joined field

    public Rating() {}

    public Rating(int id, int tripId, int raterId, int ratedId, int score, String comment) {
        this.id = id;
        this.tripId = tripId;
        this.raterId = raterId;
        this.ratedId = ratedId;
        this.score = score;
        this.comment = comment;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getRaterId() { return raterId; }
    public void setRaterId(int raterId) { this.raterId = raterId; }

    public int getRatedId() { return ratedId; }
    public void setRatedId(int ratedId) { this.ratedId = ratedId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getRaterName() { return raterName; }
    public void setRaterName(String raterName) { this.raterName = raterName; }
}
