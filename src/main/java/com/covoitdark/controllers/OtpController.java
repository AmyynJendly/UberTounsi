package com.covoitdark.controllers;

import com.covoitdark.dao.OtpDAO;
import com.covoitdark.dao.UserDAO;
import com.covoitdark.models.OtpVerification;
import com.covoitdark.utils.SessionManager;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Feature 4 – OTP-based email/phone verification.
 *
 * Flow:
 *  1. User calls generateOtp() → 6-digit code stored in otp_verifications.
 *     In a real deployment, this code would be emailed/SMS'd; here it is
 *     returned in the API response so the front-end can display it (demo mode).
 *  2. User submits the code via verifyOtp() → user.is_verified set to TRUE.
 */
public class OtpController {

    private final OtpDAO otpDAO = new OtpDAO();
    private final UserDAO userDAO = new UserDAO();

    /** Generate a new 6-digit OTP for the current user, valid for 10 minutes. */
    public String generateOtp() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == -1) return null;

        String code = String.format("%06d", new Random().nextInt(1000000));
        OtpVerification otp = new OtpVerification(userId, code, LocalDateTime.now().plusMinutes(10));
        otpDAO.create(otp);
        return code;  // In production: send via email/SMS, return only "sent" confirmation
    }

    /**
     * Verify the supplied OTP code for the current user.
     * On success marks the user as verified and the OTP as used.
     */
    public boolean verifyOtp(String code) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        System.out.println("[OTP] Verifying code '" + code + "' for user " + userId);
        
        if (userId == -1 || code == null) {
            System.err.println("[OTP] Verification failed: No active session or null code");
            return false;
        }

        OtpVerification otp = otpDAO.findLatestValid(userId);
        if (otp == null) {
            System.err.println("[OTP] Verification failed: No valid OTP found in DB for user " + userId);
            return false;
        }
        
        if (!otp.getOtpCode().equals(code)) {
            System.err.println("[OTP] Verification failed: Code mismatch. Expected '" + otp.getOtpCode() + "', got '" + code + "'");
            return false;
        }

        otpDAO.markUsed(otp.getId());
        userDAO.setVerified(userId, true);
        // Refresh session
        userDAO.findById(userId).ifPresent(SessionManager.getInstance()::setCurrentUser);
        System.out.println("[OTP] Verification SUCCESS for user " + userId);
        return true;
    }
}
