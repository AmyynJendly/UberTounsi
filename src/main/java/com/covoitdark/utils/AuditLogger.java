package com.covoitdark.utils;

import com.covoitdark.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes and reads the admin audit log.
 *
 * Demonstrates:
 *  - Collections framework: ArrayList for result lists
 *  - Map (via LinkedHashMap) for structured log entries
 *  - SOLID / Single Responsibility: only owns audit-log I/O
 */
public class AuditLogger {

    private static final AuditLogger INSTANCE = new AuditLogger();
    private AuditLogger() {}
    public static AuditLogger getInstance() { return INSTANCE; }

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Record an admin action.
     * @param actorId    the admin user id performing the action
     * @param action     short verb, e.g. "BLOCK_USER", "APPROVE_ID", "DELETE_USER"
     * @param targetType entity type, e.g. "USER", "TRIP", "DISPUTE"
     * @param targetId   id of the affected entity
     * @param detail     free-text context (name, reason, etc.)
     */
    public void log(int actorId, String action, String targetType, int targetId, String detail) {
        String sql = "INSERT INTO audit_log (actor_id, action, target_type, target_id, detail) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, actorId);
            ps.setString(2, action);
            ps.setString(3, targetType);
            ps.setInt(4, targetId);
            ps.setString(5, detail);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Convenience overload without a detail string. */
    public void log(int actorId, String action, String targetType, int targetId) {
        log(actorId, action, targetType, targetId, null);
    }

    /**
     * Return the most recent audit entries as JSON-ready maps.
     * Each entry: { id, actorId, actorName, action, targetType, targetId, detail, createdAt }
     */
    public List<java.util.Map<String, Object>> getRecentEntries(int limit) {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT a.*, u.full_name as actor_name " +
                     "FROM audit_log a JOIN users u ON a.actor_id = u.id " +
                     "ORDER BY a.created_at DESC LIMIT ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id",         rs.getInt("id"));
                m.put("actorId",    rs.getInt("actor_id"));
                m.put("actorName",  rs.getString("actor_name"));
                m.put("action",     rs.getString("action"));
                m.put("targetType", rs.getString("target_type"));
                m.put("targetId",   rs.getInt("target_id"));
                m.put("detail",     rs.getString("detail"));
                m.put("createdAt",  String.valueOf(rs.getTimestamp("created_at")));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
