package com.covoitdark.dao;

import com.covoitdark.models.OtpVerification;

import java.sql.*;
import java.time.LocalDateTime;

/** Feature 4 – Persists OTP codes for email/phone verification. */
public class OtpDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(OtpVerification otp) {
        String sql = "INSERT INTO otp_verifications (user_id, otp_code, expires_at) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, otp.getUserId());
            ps.setString(2, otp.getOtpCode());
            ps.setTimestamp(3, Timestamp.valueOf(otp.getExpiresAt()));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) otp.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Find the latest unused, unexpired OTP for a user. */
    public OtpVerification findLatestValid(int userId) {
        String sql = "SELECT * FROM otp_verifications " +
                     "WHERE user_id=? AND used=FALSE AND expires_at > ? " +
                     "ORDER BY created_at DESC LIMIT 1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean markUsed(int otpId) {
        String sql = "UPDATE otp_verifications SET used=TRUE WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, otpId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private OtpVerification mapRow(ResultSet rs) throws SQLException {
        OtpVerification otp = new OtpVerification();
        otp.setId(rs.getInt("id"));
        otp.setUserId(rs.getInt("user_id"));
        otp.setOtpCode(rs.getString("otp_code"));
        otp.setUsed(rs.getBoolean("used"));
        Timestamp exp = rs.getTimestamp("expires_at");
        if (exp != null) otp.setExpiresAt(exp.toLocalDateTime());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) otp.setCreatedAt(created.toLocalDateTime());
        return otp;
    }
}
