package com.covoitdark.models;

import java.time.LocalDateTime;

public class Block {
    private int id;
    private int blockerId;
    private int blockedId;
    private LocalDateTime createdAt;

    public Block() {}

    public Block(int id, int blockerId, int blockedId) {
        this.id = id;
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBlockerId() { return blockerId; }
    public void setBlockerId(int blockerId) { this.blockerId = blockerId; }

    public int getBlockedId() { return blockedId; }
    public void setBlockedId(int blockedId) { this.blockedId = blockedId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
