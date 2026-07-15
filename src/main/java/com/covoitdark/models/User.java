package com.covoitdark.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

    public enum Role { PASSENGER, DRIVER, ADMIN }

    private int id;
    private String fullName;
    private String email;
    private String password;
    private String salt;
    private String phone;
    private Role role;
    private String bio;
    private List<String> languages;
    private double reputationScore;
    private boolean isVerified;
    private boolean isBlocked;
    private boolean idVerified;          // Feature 5: ID document verified by admin
    private String idDocument;           // base64 CIN/passport photo submitted by driver
    private int failedLogins;
    private double balance;
    private double cancellationRate;     // Feature 6: cached cancellation rate
    private String avatar;
    private LocalDateTime createdAt;

    public User() {
        this.languages = new ArrayList<>();
        this.role = Role.PASSENGER;
        this.balance = 0.0;
    }

    public User(int id, String fullName, String email, String password, String salt,
                String phone, Role role, String bio, List<String> languages,
                double reputationScore, boolean isVerified, boolean isBlocked, double balance) {
        this.id = id;
        this.fullName = Objects.requireNonNull(fullName);
        this.email = Objects.requireNonNull(email);
        this.password = password;
        this.salt = salt;
        this.phone = phone;
        this.role = role != null ? role : Role.PASSENGER;
        this.bio = bio;
        this.languages = languages != null ? new ArrayList<>(languages) : new ArrayList<>();
        this.reputationScore = reputationScore;
        this.isVerified = isVerified;
        this.isBlocked = isBlocked;
        this.balance = balance;
    }

    // Getters
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getSalt() { return salt; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public String getBio() { return bio; }
    public List<String> getLanguages() { return new ArrayList<>(languages); }
    public double getReputationScore() { return reputationScore; }
    public boolean isVerified() { return isVerified; }
    public boolean isBlocked() { return isBlocked; }
    public int getFailedLogins() { return failedLogins; }
    public String getAvatar() { return avatar; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setSalt(String salt) { this.salt = salt; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(Role role) { this.role = role != null ? role : Role.PASSENGER; }
    public void setBio(String bio) { this.bio = bio; }
    public void setLanguages(List<String> languages) {
        this.languages = languages != null ? new ArrayList<>(languages) : new ArrayList<>();
    }
    public void setReputationScore(double score) { this.reputationScore = Math.max(0, Math.min(5, score)); }
    public void setVerified(boolean verified) { this.isVerified = verified; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }
    public void setFailedLogins(int failedLogins) { this.failedLogins = failedLogins; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Helpers
    public boolean isIdVerified() { return idVerified; }
    public void setIdVerified(boolean idVerified) { this.idVerified = idVerified; }

    public String getIdDocument() { return idDocument; }
    public void setIdDocument(String idDocument) { this.idDocument = idDocument; }

    public double getCancellationRate() { return cancellationRate; }
    public void setCancellationRate(double cancellationRate) { this.cancellationRate = cancellationRate; }

    public boolean isDriver() { return role == Role.DRIVER; }
    public boolean isPassenger() { return role == Role.PASSENGER; }
    public boolean isAdmin() { return role == Role.ADMIN; }
    public String getLanguagesString() { return String.join(",", languages); }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return id == u.id && Objects.equals(email, u.email);
    }

    @Override
    public int hashCode() { return Objects.hash(id, email); }

    @Override
    public String toString() {
        return "User{id=" + id + ", fullName='" + fullName + "', role=" + role + "}";
    }
}
