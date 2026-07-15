package com.covoitdark.dao;

import com.covoitdark.models.SavedSearch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavedSearchDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean create(SavedSearch search) {
        String sql = "INSERT INTO saved_searches (passenger_id, name, departure, arrival, preferred_time, frequency, start_date, end_date) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, search.getPassengerId());
            ps.setString(2, search.getName());
            ps.setString(3, search.getDeparture());
            ps.setString(4, search.getArrival());
            ps.setObject(5, search.getPreferredTime() != null ? Time.valueOf(search.getPreferredTime()) : null);
            ps.setString(6, search.getFrequency());
            ps.setObject(7, search.getStartDate() != null ? Date.valueOf(search.getStartDate()) : null);
            ps.setObject(8, search.getEndDate() != null ? Date.valueOf(search.getEndDate()) : null);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) search.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<SavedSearch> findByPassenger(int passengerId) {
        List<SavedSearch> list = new ArrayList<>();
        String sql = "SELECT * FROM saved_searches WHERE passenger_id=? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Integer> findMatchingPassengers(String departure, String arrival) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT passenger_id FROM saved_searches WHERE departure=? AND arrival=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, departure);
            ps.setString(2, arrival);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getInt("passenger_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM saved_searches WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private SavedSearch mapRow(ResultSet rs) throws SQLException {
        SavedSearch s = new SavedSearch();
        s.setId(rs.getInt("id"));
        s.setPassengerId(rs.getInt("passenger_id"));
        s.setName(rs.getString("name"));
        s.setDeparture(rs.getString("departure"));
        s.setArrival(rs.getString("arrival"));
        Time pt = rs.getTime("preferred_time");
        if (pt != null) s.setPreferredTime(pt.toLocalTime());
        s.setFrequency(rs.getString("frequency"));
        Date sd = rs.getDate("start_date");
        if (sd != null) s.setStartDate(sd.toLocalDate());
        Date ed = rs.getDate("end_date");
        if (ed != null) s.setEndDate(ed.toLocalDate());
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }
}
