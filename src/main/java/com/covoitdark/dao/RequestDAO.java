package com.covoitdark.dao;

import com.covoitdark.models.Request;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Request findById(int id) {
        String sql = "SELECT r.*, u.full_name as passenger_name FROM requests r JOIN users u ON r.passenger_id=u.id WHERE r.id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Request> findByTrip(int tripId) {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as passenger_name FROM requests r JOIN users u ON r.passenger_id=u.id WHERE r.trip_id=? ORDER BY r.created_at";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Request> findByPassenger(int passengerId) {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as passenger_name FROM requests r JOIN users u ON r.passenger_id=u.id WHERE r.passenger_id=? ORDER BY r.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean create(Request request) {
        String sql = "INSERT INTO requests (trip_id, passenger_id, quantity, total_cost, status, secret_code) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, request.getTripId());
            ps.setInt(2, request.getPassengerId());
            ps.setInt(3, request.getQuantity());
            ps.setDouble(4, request.getTotalCost());
            ps.setString(5, request.getStatus().name());
            ps.setString(6, request.getSecretCode());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) request.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM requests WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteArchivedByPassenger(int passengerId) {
        String sql = "DELETE FROM requests WHERE passenger_id=? AND status IN ('CANCELLED', 'REJECTED')";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Request> findByDriver(int driverId) {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as passenger_name FROM requests r " +
                    "JOIN trips t ON r.trip_id = t.id " +
                    "JOIN users u ON r.passenger_id = u.id " +
                    "WHERE t.driver_id = ? ORDER BY r.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStatus(int requestId, Request.Status status) {
        String sql = "UPDATE requests SET status=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Request> findAll() {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as passenger_name FROM requests r JOIN users u ON r.passenger_id=u.id ORDER BY r.created_at DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateCounterPrice(int requestId, double counterPrice) {
        String sql = "UPDATE requests SET counter_price=?, status='NEGOTIATING' WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, counterPrice);
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean existsForPassengerAndTrip(int passengerId, int tripId) {
        String sql = "SELECT COUNT(*) FROM requests WHERE passenger_id=? AND trip_id=? AND status != 'CANCELLED'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            ps.setInt(2, tripId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Request> findPendingByTrip(int tripId) {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as passenger_name FROM requests r JOIN users u ON r.passenger_id=u.id WHERE r.trip_id=? AND r.status='PENDING'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public double getReservedAmount(int driverId) {
        String sql = "SELECT SUM(r.total_cost) FROM requests r " +
                     "JOIN trips t ON r.trip_id = t.id " +
                     "WHERE t.driver_id = ? AND r.status = 'ACCEPTED' AND t.status = 'ACTIVE'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private Request mapRow(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId(rs.getInt("id"));
        r.setTripId(rs.getInt("trip_id"));
        r.setPassengerId(rs.getInt("passenger_id"));
        r.setQuantity(rs.getInt("quantity"));
        r.setTotalCost(rs.getDouble("total_cost"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            r.setStatus(Request.Status.valueOf(statusStr));
        }
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            r.setCreatedAt(ts.toLocalDateTime());
        }
        
        try {
            r.setPassengerName(rs.getString("passenger_name"));
        } catch (SQLException e) {
            // Ignored
        }

        try {
            r.setSecretCode(rs.getString("secret_code"));
        } catch (SQLException e) {
            // Ignored
        }
        
        try {
            r.setCounterPrice(rs.getDouble("counter_price"));
        } catch (SQLException e) {
            // Ignored
        }
        
        try {
            r.setLuggageSize(rs.getString("luggage_size"));
        } catch (SQLException e) {
            // Ignored
        }

        return r;
    }
}