package com.covoitdark.dao;

import com.covoitdark.models.Rating;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(Rating rating) {
        String sql = "INSERT INTO ratings (trip_id, rater_id, rated_id, score, comment) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rating.getTripId());
            ps.setInt(2, rating.getRaterId());
            ps.setInt(3, rating.getRatedId());
            ps.setInt(4, rating.getScore());
            ps.setString(5, rating.getComment());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) rating.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Rating> findByRatedUser(int ratedId) {
        List<Rating> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as rater_name FROM ratings r JOIN users u ON r.rater_id=u.id WHERE r.rated_id=? ORDER BY r.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, ratedId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public double getAverageScore(int userId) {
        String sql = "SELECT AVG(score) FROM ratings WHERE rated_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public boolean hasRated(int raterId, int tripId, int ratedId) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE rater_id=? AND trip_id=? AND rated_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, raterId);
            ps.setInt(2, tripId);
            ps.setInt(3, ratedId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Rating mapRow(ResultSet rs) throws SQLException {
        Rating r = new Rating();
        r.setId(rs.getInt("id"));
        r.setTripId(rs.getInt("trip_id"));
        r.setRaterId(rs.getInt("rater_id"));
        r.setRatedId(rs.getInt("rated_id"));
        r.setScore(rs.getInt("score"));
        r.setComment(rs.getString("comment"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        try { r.setRaterName(rs.getString("rater_name")); } catch (SQLException ignored) {}
        return r;
    }
}
