package com.covoitdark.dao;

import com.covoitdark.models.Stats;

import java.sql.*;

public class StatsDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Stats findByUser(int userId) {
        String sql = "SELECT * FROM stats WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean create(int userId) {
        String sql = "INSERT IGNORE INTO stats (user_id) VALUES (?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Stats stats) {
        String sql = "UPDATE stats SET total_trips=?, money_saved=?, co2_saved=? WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, stats.getTotalTrips());
            ps.setDouble(2, stats.getMoneySaved());
            ps.setDouble(3, stats.getCo2Saved());
            ps.setInt(4, stats.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public void incrementTrips(int userId, int count) {
        create(userId); // ensure row exists
        String sql = "UPDATE stats SET total_trips = total_trips + ? WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addCO2(int userId, double co2kg) {
        create(userId);
        String sql = "UPDATE stats SET co2_saved = co2_saved + ? WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, co2kg);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addMoneyEarned(int userId, double amount) {
        create(userId);
        String sql = "UPDATE stats SET money_earned = money_earned + ? WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addMoneySaved(int userId, double amount) {
        create(userId);
        String sql = "UPDATE stats SET money_saved = money_saved + ? WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void incrementCancelled(int userId) {
        create(userId);
        String sql = "UPDATE stats SET cancelled_trips = cancelled_trips + 1 WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Stats mapRow(ResultSet rs) throws SQLException {
        Stats s = new Stats();
        s.setUserId(rs.getInt("user_id"));
        s.setId(s.getUserId());
        s.setTotalTrips(rs.getInt("total_trips"));
        s.setMoneySaved(rs.getDouble("money_saved"));
        s.setCo2Saved(rs.getDouble("co2_saved"));
        try { s.setMoneyEarned(rs.getDouble("money_earned")); } catch (SQLException ignored) {}
        try { s.setCancelledTrips(rs.getInt("cancelled_trips")); } catch (SQLException ignored) {}
        return s;
    }
}
