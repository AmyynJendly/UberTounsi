package com.covoitdark.dao;

import com.covoitdark.models.Car;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Car findById(int id) {
        String sql = "SELECT * FROM cars WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Car> findByDriver(int driverId) {
        List<Car> list = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE driver_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean create(Car car) {
        String sql = "INSERT INTO cars (driver_id, brand, model, color, seats, license_plate, image_base64) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, car.getDriverId());
            ps.setString(2, car.getBrand());
            ps.setString(3, car.getModel());
            ps.setString(4, car.getColor());
            ps.setInt(5, car.getSeats());
            ps.setString(6, car.getLicensePlate());
            ps.setString(7, car.getImageBase64());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) car.setId(keys.getInt(1));
                System.out.println("SQL: INSERT car OK, ID=" + car.getId());
                return true;
            }
        } catch (SQLException e) { 
            System.err.println("SQL ERROR in CarDAO.create: " + e.getMessage());
            e.printStackTrace(); 
        }
        return false;
    }

    public boolean delete(int carId) {
        String sql = "DELETE FROM cars WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, carId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Car car) {
        String sql = "UPDATE cars SET brand=?, model=?, color=?, seats=?, license_plate=?, image_base64=? WHERE id=? AND driver_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, car.getBrand());
            ps.setString(2, car.getModel());
            ps.setString(3, car.getColor());
            ps.setInt(4, car.getSeats());
            ps.setString(5, car.getLicensePlate());
            ps.setString(6, car.getImageBase64());
            ps.setInt(7, car.getId());
            ps.setInt(8, car.getDriverId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean licensePlateExists(String plate) {
        String sql = "SELECT COUNT(*) FROM cars WHERE license_plate = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Car mapRow(ResultSet rs) throws SQLException {
        return new Car(
            rs.getInt("id"),
            rs.getInt("driver_id"),
            rs.getString("brand"),
            rs.getString("model"),
            rs.getString("color"),
            rs.getInt("seats"),
            rs.getString("license_plate"),
            rs.getString("image_base64")
        );
    }
}
