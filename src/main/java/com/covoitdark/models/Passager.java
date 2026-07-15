package com.covoitdark.models;

import java.util.List;

public class Passager extends User {

    public Passager() {
        super();
        setRole(Role.PASSENGER);
    }

    public Passager(int id, String fullName, String email, String password, String salt,
                    String phone, String bio, List<String> languages,
                    double reputationScore, boolean isVerified, boolean isBlocked, double balance) {
        super(id, fullName, email, password, salt, phone, Role.PASSENGER, bio, languages, reputationScore, isVerified, isBlocked, balance);
    }
}
