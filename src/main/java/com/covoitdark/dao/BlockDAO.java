package com.covoitdark.dao;

import com.covoitdark.models.Block;

import java.sql.*;

public class BlockDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(Block block) {
        String sql = "INSERT INTO blocks (blocker_id, blocked_id) VALUES (?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, block.getBlockerId());
            ps.setInt(2, block.getBlockedId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) block.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean isBlocked(int blockerId, int blockedId) {
        String sql = "SELECT COUNT(*) FROM blocks WHERE blocker_id=? AND blocked_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, blockerId);
            ps.setInt(2, blockedId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int blockerId, int blockedId) {
        String sql = "DELETE FROM blocks WHERE blocker_id=? AND blocked_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, blockerId);
            ps.setInt(2, blockedId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
