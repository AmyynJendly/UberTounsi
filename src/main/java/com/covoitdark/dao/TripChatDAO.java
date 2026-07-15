package com.covoitdark.dao;

import com.covoitdark.models.TripChatMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Feature 3 – DAO for trip group chat messages. */
public class TripChatDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(TripChatMessage msg) {
        String sql = "INSERT INTO trip_chat_messages (trip_id, sender_id, content) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, msg.getTripId());
            ps.setInt(2, msg.getSenderId());
            ps.setString(3, msg.getContent());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) msg.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<TripChatMessage> findByTrip(int tripId) {
        List<TripChatMessage> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name as sender_name " +
                     "FROM trip_chat_messages m JOIN users u ON m.sender_id=u.id " +
                     "WHERE m.trip_id=? ORDER BY m.sent_at ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private TripChatMessage mapRow(ResultSet rs) throws SQLException {
        TripChatMessage m = new TripChatMessage();
        m.setId(rs.getInt("id"));
        m.setTripId(rs.getInt("trip_id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setContent(rs.getString("content"));
        try { m.setSenderName(rs.getString("sender_name")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("sent_at");
        if (ts != null) m.setSentAt(ts.toLocalDateTime());
        return m;
    }
}
