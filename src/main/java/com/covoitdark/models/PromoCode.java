package com.covoitdark.models;

import java.time.LocalDateTime;

/** Feature 12 – Promotional discount code created by admins. */
public class PromoCode {

    private int id;
    private String code;
    private double discountPct;    // e.g. 0.10 = 10 %
    private double discountFixed;  // e.g. 5.0 = 5 TND off
    private int maxUses;
    private int usedCount;
    private LocalDateTime expiresAt;
    private boolean isActive;
    private LocalDateTime createdAt;

    public PromoCode() { this.isActive = true; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountPct() { return discountPct; }
    public void setDiscountPct(double discountPct) { this.discountPct = discountPct; }

    public double getDiscountFixed() { return discountFixed; }
    public void setDiscountFixed(double discountFixed) { this.discountFixed = discountFixed; }

    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isValid() {
        return isActive
                && usedCount < maxUses
                && (expiresAt == null || LocalDateTime.now().isBefore(expiresAt));
    }

    /** Apply this code to a base price and return the discounted price. */
    public double applyTo(double basePrice) {
        double after = basePrice - discountFixed;
        after = after * (1.0 - discountPct);
        return Math.max(0, after);
    }
}
