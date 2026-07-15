package com.covoitdark.dao;

import com.covoitdark.models.MoyenPaiement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoyenPaiementDAO {
    
    public boolean add(MoyenPaiement mp) {
        String sql = "INSERT INTO payment_methods (user_id, type, last_four, brand, expiry) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, mp.getUserId());
            ps.setString(2, mp.getType());
            ps.setString(3, mp.getLastFour());
            ps.setString(4, mp.getBrand());
            ps.setString(5, mp.getExpiry());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) mp.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<MoyenPaiement> listByUser(int userId) {
        List<MoyenPaiement> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_methods WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MoyenPaiement mp = new MoyenPaiement(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("last_four"),
                        rs.getString("brand"),
                        rs.getString("expiry")
                    );
                    mp.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(mp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM payment_methods WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
