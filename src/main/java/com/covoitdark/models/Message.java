package com.covoitdark.models;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int requestId;
    private int senderId;
    private String content;
    private boolean isQuickResponse;
    private LocalDateTime sentAt;
    private String senderName; // Joined field
    private int receiverId;

    public Message() {}

    public Message(int id, int requestId, int senderId, int receiverId, String content, boolean isQuickResponse) {
        this.id = id;
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.isQuickResponse = isQuickResponse;
        this.sentAt = LocalDateTime.now();
    }

    public Message(int id, int requestId, int senderId, String content, boolean isQuickResponse) {
        this(id, requestId, senderId, 0, content, isQuickResponse);
    }

    public Message(int id, int requestId, int senderId, String content, boolean isQuickResponse, LocalDateTime sentAt) {
        this.id = id;
        this.requestId = requestId;
        this.senderId = senderId;
        this.content = content;
        this.isQuickResponse = isQuickResponse;
        this.sentAt = sentAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isQuickResponse() { return isQuickResponse; }
    public void setQuickResponse(boolean quickResponse) { isQuickResponse = quickResponse; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
}
