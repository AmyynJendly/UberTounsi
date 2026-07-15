package com.covoitdark.dao;

import com.covoitdark.models.PromoCode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Feature 12 – DAO for admin-managed promo codes. */
public class PromoCodeDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(PromoCode p) {
        String sql = "INSERT INTO promo_codes (code, discount_pct, discount_fixed, max_uses, expires_at, is_active) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCode().toUpperCase());
            ps.setDouble(2, p.getDiscountPct());
            ps.setDouble(3, p.getDiscountFixed());
            ps.setInt(4, p.getMaxUses());
            ps.setTimestamp(5, p.getExpiresAt() != null ? Timestamp.valueOf(p.getExpiresAt()) : null);
            ps.setBoolean(6, p.isActive());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) p.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public PromoCode findByCode(String code) {
        String sql = "SELECT * FROM promo_codes WHERE code=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<PromoCode> findAll() {
        List<PromoCode> list = new ArrayList<>();
        String sql = "SELECT * FROM promo_codes ORDER BY created_at DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean incrementUsed(int promoId) {
        String sql = "UPDATE promo_codes SET used_count = used_count + 1 WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, promoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deactivate(int promoId) {
        String sql = "UPDATE promo_codes SET is_active=FALSE WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, promoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private PromoCode mapRow(ResultSet rs) throws SQLException {
        PromoCode p = new PromoCode();
        p.setId(rs.getInt("id"));
        p.setCode(rs.getString("code"));
        p.setDiscountPct(rs.getDouble("discount_pct"));
        p.setDiscountFixed(rs.getDouble("discount_fixed"));
        p.setMaxUses(rs.getInt("max_uses"));
        p.setUsedCount(rs.getInt("used_count"));
        p.setActive(rs.getBoolean("is_active"));
        Timestamp exp = rs.getTimestamp("expires_at");
        if (exp != null) p.setExpiresAt(exp.toLocalDateTime());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        return p;
    }
}
