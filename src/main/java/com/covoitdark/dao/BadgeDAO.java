package com.covoitdark.dao;

import com.covoitdark.models.Badge;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BadgeDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(Badge badge) {
        String sql = "INSERT INTO badges (user_id, badge_name) VALUES (?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, badge.getUserId());
            ps.setString(2, badge.getBadgeName());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) badge.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Badge> findByUser(int userId) {
        List<Badge> list = new ArrayList<>();
        String sql = "SELECT * FROM badges WHERE user_id=? ORDER BY awarded_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean hasBadge(int userId, String badgeName) {
        String sql = "SELECT COUNT(*) FROM badges WHERE user_id=? AND badge_name=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, badgeName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Badge mapRow(ResultSet rs) throws SQLException {
        Badge b = new Badge();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setBadgeName(rs.getString("badge_name"));
        Timestamp ts = rs.getTimestamp("awarded_at");
        if (ts != null) b.setAwardedAt(ts.toLocalDateTime());
        return b;
    }
}
