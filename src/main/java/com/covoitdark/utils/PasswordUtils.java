package com.covoitdark.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing (SHA-256 + random salt) and strength validation.
 */
public class PasswordUtils {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private PasswordUtils() {}

    /** Generate a secure random salt (Base64 encoded). */
    public static String generateSalt() {
        byte[] saltBytes = new byte[32];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /** Hash password with given salt using SHA-256. */
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashed = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** Verify a plain password against the stored hash and salt. */
    public static boolean verify(String plainPassword, String storedHash, String salt) {
        return hash(plainPassword, salt).equals(storedHash);
    }

    public enum PasswordStrength { WEAK, MEDIUM, STRONG }

    /** Evaluate password strength. */
    public static PasswordStrength checkStrength(String password) {
        if (password == null || password.length() < MIN_LENGTH) return PasswordStrength.WEAK;
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()-_=+[]{}|;:',.<>?/".indexOf(c) >= 0);
        int score = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        if (score >= 4 && password.length() >= 12) return PasswordStrength.STRONG;
        if (score >= 3) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }

    /** Validate password meets minimum requirements. */
    public static String validate(String password) {
        if (password == null || password.isBlank()) return "Le mot de passe est requis.";
        if (password.length() < MIN_LENGTH) return "Au moins " + MIN_LENGTH + " caractères requis.";
        if (checkStrength(password) == PasswordStrength.WEAK)
            return "Mot de passe trop faible. Ajoutez des chiffres ou caractères spéciaux.";
        return null; // valid
    }

    public static int getMaxFailedAttempts() { return MAX_FAILED_ATTEMPTS; }
}
