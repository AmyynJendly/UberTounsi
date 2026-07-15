package com.covoitdark.models;

import java.time.LocalDateTime;

/** Feature 3 – Per-trip group chat message. */
public class TripChatMessage {

    private int id;
    private int tripId;
    private int senderId;
    private String senderName;   // joined
    private String content;
    private LocalDateTime sentAt;

    public TripChatMessage() {}

    public TripChatMessage(int tripId, int senderId, String content) {
        this.tripId = tripId;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
