package com.covoitdark.dao;

import com.covoitdark.models.DriverFavorite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Feature 7 – DAO for passenger-saved favorite drivers. */
public class DriverFavoriteDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean add(int passengerId, int driverId) {
        String sql = "INSERT IGNORE INTO driver_favorites (passenger_id, driver_id) VALUES (?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            ps.setInt(2, driverId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean remove(int passengerId, int driverId) {
        String sql = "DELETE FROM driver_favorites WHERE passenger_id=? AND driver_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            ps.setInt(2, driverId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean isFavorite(int passengerId, int driverId) {
        String sql = "SELECT COUNT(*) FROM driver_favorites WHERE passenger_id=? AND driver_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            ps.setInt(2, driverId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<DriverFavorite> findByPassenger(int passengerId) {
        List<DriverFavorite> list = new ArrayList<>();
        String sql = "SELECT f.*, u.full_name as driver_name " +
                     "FROM driver_favorites f JOIN users u ON f.driver_id=u.id " +
                     "WHERE f.passenger_id=? ORDER BY f.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Returns all passenger ids who have saved driverId as a favorite. */
    public List<Integer> findPassengersByDriver(int driverId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT passenger_id FROM driver_favorites WHERE driver_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("passenger_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    private DriverFavorite mapRow(ResultSet rs) throws SQLException {
        DriverFavorite f = new DriverFavorite();
        f.setId(rs.getInt("id"));
        f.setPassengerId(rs.getInt("passenger_id"));
        f.setDriverId(rs.getInt("driver_id"));
        try { f.setDriverName(rs.getString("driver_name")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) f.setCreatedAt(ts.toLocalDateTime());
        return f;
    }
}
