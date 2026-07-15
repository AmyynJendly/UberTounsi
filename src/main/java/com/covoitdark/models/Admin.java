package com.covoitdark.models;

import java.util.List;

public class Admin extends User {

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(int id, String fullName, String email, String password, String salt,
                 String phone, String bio, List<String> languages,
                 double reputationScore, boolean isVerified, boolean isBlocked, double balance) {
        super(id, fullName, email, password, salt, phone, Role.ADMIN, bio, languages, reputationScore, isVerified, isBlocked, balance);
    }
}
