package com.covoitdark.models;

import java.time.LocalDateTime;

/** Feature 4 – OTP record for email/phone verification. */
public class OtpVerification {

    private int id;
    private int userId;
    private String otpCode;
    private LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime createdAt;

    public OtpVerification() {}

    public OtpVerification(int userId, String otpCode, LocalDateTime expiresAt) {
        this.userId = userId;
        this.otpCode = otpCode;
        this.expiresAt = expiresAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
}
