package com.covoitdark.dao;

import com.covoitdark.models.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(Message message) {
        String sql = "INSERT INTO messages (request_id, sender_id, receiver_id, content, is_quick_response) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (message.getRequestId() > 0) ps.setInt(1, message.getRequestId());
            else ps.setNull(1, Types.INTEGER);
            ps.setInt(2, message.getSenderId());
            ps.setInt(3, message.getReceiverId());
            ps.setString(4, message.getContent());
            ps.setBoolean(5, message.isQuickResponse());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) message.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Message> findByRequest(int requestId) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name as sender_name FROM messages m JOIN users u ON m.sender_id=u.id WHERE m.request_id=? ORDER BY m.sent_at ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Message> findConversation(int userId, int otherId) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name as sender_name FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                     "OR (m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.sent_at ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, otherId);
            ps.setInt(3, otherId);
            ps.setInt(4, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setRequestId(rs.getInt("request_id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        m.setContent(rs.getString("content"));
        m.setQuickResponse(rs.getBoolean("is_quick_response"));
        Timestamp ts = rs.getTimestamp("sent_at");
        if (ts != null) m.setSentAt(ts.toLocalDateTime());
        try { m.setSenderName(rs.getString("sender_name")); } catch (SQLException ignored) {}
        return m;
    }
}
