package com.covoitdark.dao;

import com.covoitdark.models.Report;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    public boolean create(Report report) {
        String sql = "INSERT INTO reports (reporter_id, reported_id, reason) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, report.getReporterId()); ps.setInt(2, report.getReportedId());
            ps.setString(3, report.getReason());
            if (ps.executeUpdate() > 0) {
                ResultSet k = ps.getGeneratedKeys(); if (k.next()) report.setId(k.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Report> findAll() {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT r.*, u1.full_name as reporter_name, u2.full_name as reported_name FROM reports r JOIN users u1 ON r.reporter_id=u1.id JOIN users u2 ON r.reported_id=u2.id ORDER BY r.created_at DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM reports WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Report> findByReported(int reportedId) {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT r.*, u1.full_name as reporter_name, u2.full_name as reported_name FROM reports r JOIN users u1 ON r.reporter_id=u1.id JOIN users u2 ON r.reported_id=u2.id WHERE r.reported_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, reportedId); ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Report mapRow(ResultSet rs) throws SQLException {
        Report r = new Report(rs.getInt("id"), rs.getInt("reporter_id"), rs.getInt("reported_id"), rs.getString("reason"));
        Timestamp ts = rs.getTimestamp("created_at"); if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        try { r.setReporterName(rs.getString("reporter_name")); } catch (SQLException ignored) {}
        try { r.setReportedName(rs.getString("reported_name")); } catch (SQLException ignored) {}
        return r;
    }
}
