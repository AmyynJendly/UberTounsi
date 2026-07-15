package com.covoitdark.controllers;

import com.covoitdark.models.MoyenPaiement;
import com.covoitdark.models.User;
import com.covoitdark.dao.UserDAO;
import com.covoitdark.dao.StatsDAO;
import com.covoitdark.utils.SessionManager;

public class PaymentController {

    private final UserDAO userDAO = new UserDAO();
    private final StatsDAO statsDAO = new StatsDAO();

    /**
     * Authorize payment: verify the user has sufficient balance (does not deduct yet).
     */
    public boolean payer(int userId, double amount) {
        if (amount <= 0) return false;
        User user = userDAO.findByIdDirect(userId);
        if (user == null || user.isBlocked()) return false;
        if (user.getBalance() < amount) return false;
        return true;
    }

    /**
     * Capture payment: actually deduct from passenger and credit driver on acceptance.
     */
    public boolean capturerPaiement(int passengerId, int driverId, double amount) {
        if (amount <= 0) return false;
        User passenger = userDAO.findByIdDirect(passengerId);
        User driver = userDAO.findByIdDirect(driverId);
        if (passenger == null || passenger.getBalance() < amount) return false;
        boolean debited = userDAO.updateBalance(passengerId, passenger.getBalance() - amount);
        if (debited && driver != null) {
            userDAO.updateBalance(driverId, driver.getBalance() + amount);
            statsDAO.addMoneyEarned(driverId, amount);   // Feature 10: track driver earnings
        }
        return debited;
    }

    /**
     * Refund: add amount back to passenger wallet.
     */
    public boolean rembourser(int passengerId, double amount) {
        if (amount <= 0) return true;
        User passenger = userDAO.findByIdDirect(passengerId);
        if (passenger == null) return false;
        return userDAO.updateBalance(passengerId, passenger.getBalance() + amount);
    }

    public MoyenPaiement getMoyenPaiement() {
        // Return default payment method for current user
        int userId = SessionManager.getInstance().getCurrentUserId();
        return new MoyenPaiement(1, userId, MoyenPaiement.Type.WALLET, "Internal Wallet", null, true);
    }
}
