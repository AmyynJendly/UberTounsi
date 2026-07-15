package com.covoitdark.models;

import java.util.List;

public class Chauffeur extends User {

    public Chauffeur() {
        super();
        setRole(Role.DRIVER);
    }

    public Chauffeur(int id, String fullName, String email, String password, String salt,
                     String phone, String bio, List<String> languages,
                     double reputationScore, boolean isVerified, boolean isBlocked, double balance) {
        super(id, fullName, email, password, salt, phone, Role.DRIVER, bio, languages, reputationScore, isVerified, isBlocked, balance);
    }

    // Role-specific helper
    public boolean canProposeTrip() {
        return !isBlocked() && isVerified();
    }
}
