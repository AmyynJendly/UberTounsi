package com.covoitdark.dao;

import com.covoitdark.models.Dispute;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Feature 13 – DAO for payment disputes. */
public class DisputeDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(Dispute d) {
        String sql = "INSERT INTO disputes (trip_id, complainant_id, reason) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getTripId());
            ps.setInt(2, d.getComplainantId());
            ps.setString(3, d.getReason());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) d.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public Dispute findById(int id) {
        String sql = "SELECT d.*, u.full_name as complainant_name FROM disputes d " +
                     "JOIN users u ON d.complainant_id=u.id WHERE d.id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Dispute> findAll() {
        List<Dispute> list = new ArrayList<>();
        String sql = "SELECT d.*, u.full_name as complainant_name FROM disputes d " +
                     "JOIN users u ON d.complainant_id=u.id ORDER BY d.created_at DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Dispute> findOpen() {
        List<Dispute> list = new ArrayList<>();
        String sql = "SELECT d.*, u.full_name as complainant_name FROM disputes d " +
                     "JOIN users u ON d.complainant_id=u.id WHERE d.status='OPEN' ORDER BY d.created_at ASC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean resolve(int disputeId, String adminNote, Dispute.Status status) {
        String sql = "UPDATE disputes SET status=?, admin_note=?, resolved_at=NOW() WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, adminNote);
            ps.setInt(3, disputeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Dispute mapRow(ResultSet rs) throws SQLException {
        Dispute d = new Dispute();
        d.setId(rs.getInt("id"));
        d.setTripId(rs.getInt("trip_id"));
        d.setComplainantId(rs.getInt("complainant_id"));
        d.setReason(rs.getString("reason"));
        d.setStatus(Dispute.Status.valueOf(rs.getString("status")));
        d.setAdminNote(rs.getString("admin_note"));
        try { d.setComplainantName(rs.getString("complainant_name")); } catch (SQLException ignored) {}
        Timestamp resolved = rs.getTimestamp("resolved_at");
        if (resolved != null) d.setResolvedAt(resolved.toLocalDateTime());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) d.setCreatedAt(created.toLocalDateTime());
        return d;
    }
}
