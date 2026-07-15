package com.covoitdark.models;

import java.util.Objects;

public class Card {

    private int id;
    private int userId;
    private String cardNumber;
    private String cardHolderName;
    private String expirationDate; // format MM/YY
    private String cvc;
    private String cardBrand; // Visa, Mastercard, etc.

    public Card() {}

    public Card(int id, int userId, String cardNumber, String cardHolderName, String expirationDate, String cvc) {
        this.id = id;
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationDate = expirationDate;
        this.cvc = cvc;
        this.cardBrand = detectBrand(cardNumber);
    }

    public static boolean verifyLuhn(String number) {
        if (number == null || number.isEmpty()) return false;
        number = number.replaceAll("\\s+", "");
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private String detectBrand(String number) {
        if (number == null) return "Unknown";
        number = number.replaceAll("\\s+", "");
        if (number.startsWith("4")) return "Visa";
        if (number.startsWith("5")) return "Mastercard";
        if (number.startsWith("3")) return "Amex";
        return "Unknown";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { 
        this.cardNumber = cardNumber; 
        this.cardBrand = detectBrand(cardNumber);
    }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getCvc() { return cvc; }
    public void setCvc(String cvc) { this.cvc = cvc; }

    public String getCardBrand() { return cardBrand; }

    public String getLast4Digits() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        String clean = cardNumber.replaceAll("\\s+", "");
        return clean.substring(clean.length() - 4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return id == card.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
