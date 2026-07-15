package com.covoitdark.controllers;

import com.covoitdark.dao.PromoCodeDAO;
import com.covoitdark.models.PromoCode;
import com.covoitdark.utils.SessionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feature 12 – Promotional discount codes.
 *
 * Admin creates codes; passengers apply them at booking time.
 * Demonstrates:
 *  - Stream + map: transforms PromoCode list to a discount summary
 *  - Open/Closed Principle: new discount strategies extend PromoCode.applyTo()
 */
public class PromoCodeController {

    private final PromoCodeDAO promoCodeDAO = new PromoCodeDAO();

    /** Admin: create a new promo code. */
    public String createCode(String code, double discountPct, double discountFixed,
                             int maxUses, LocalDateTime expiresAt) {
        if (!SessionManager.getInstance().isAdmin()) return "Accès réservé à l'administrateur.";
        if (code == null || code.isBlank()) return "Code invalide.";

        PromoCode p = new PromoCode();
        p.setCode(code.trim().toUpperCase());
        p.setDiscountPct(Math.max(0, Math.min(1.0, discountPct)));
        p.setDiscountFixed(Math.max(0, discountFixed));
        p.setMaxUses(maxUses > 0 ? maxUses : 100);
        p.setExpiresAt(expiresAt);

        return promoCodeDAO.create(p) ? "Code promo créé : " + p.getCode() : "Erreur (code déjà existant ?).";
    }

    /**
     * Apply a promo code to a base price. Returns the discounted price.
     * Increments the usage counter on success.
     */
    public double applyCode(String code, double basePrice) {
        if (code == null || code.isBlank()) return basePrice;
        PromoCode promo = promoCodeDAO.findByCode(code);
        if (promo == null || !promo.isValid()) return basePrice;
        double discounted = promo.applyTo(basePrice);
        promoCodeDAO.incrementUsed(promo.getId());
        return discounted;
    }

    /** Validate without consuming — returns null if invalid, the PromoCode otherwise. */
    public PromoCode validate(String code) {
        if (code == null || code.isBlank()) return null;
        PromoCode promo = promoCodeDAO.findByCode(code);
        return (promo != null && promo.isValid()) ? promo : null;
    }

    /** Admin: list all codes with their usage summary (Stream.map demo). */
    public List<String> getSummary() {
        return promoCodeDAO.findAll().stream()
                .map(p -> p.getId() + " | " + p.getCode() + " | -" + (int)(p.getDiscountPct()*100) + "% / -"
                        + String.format("%.2f", p.getDiscountFixed()) + " TND | "
                        + p.getUsedCount() + "/" + p.getMaxUses() + " utilisations"
                        + (p.isValid() ? " [ACTIF]" : " [EXPIRÉ]"))
                .collect(Collectors.toList());
    }

    /** Admin: deactivate a code. */
    public boolean deactivate(int promoId) {
        if (!SessionManager.getInstance().isAdmin()) return false;
        return promoCodeDAO.deactivate(promoId);
    }
}
