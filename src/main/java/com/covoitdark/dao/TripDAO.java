package com.covoitdark.dao;

import com.covoitdark.models.Trip;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TripDAO — implements IRepository<Trip, Integer>.
 *
 * Demonstrates:
 *  - SOLID / Single Responsibility: only handles Trip persistence
 *  - SOLID / Dependency Inversion: implements IRepository abstraction
 *  - Java Generics: IRepository<Trip, Integer>
 *  - Stream API: findWithMinSeats(), findByMaxPrice()
 *  - Collections: ArrayList for all result sets
 */
public class TripDAO implements IRepository<Trip, Integer> {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private static final String SELECT_BASE =
        "SELECT t.*, u.full_name as driver_name, u.reputation_score as driver_rating, " +
        "CONCAT(c.brand,' ',c.model) as car_info, u.avatar as driver_avatar, " +
        "c.image_base64 as car_image, c.color as car_color, c.license_plate as car_plate, " +
        "c.seats as total_seats " +
        "FROM trips t JOIN users u ON t.driver_id=u.id JOIN cars c ON t.car_id=c.id ";

    // ── IRepository ──────────────────────────────────────────────────────────

    @Override
    public Optional<Trip> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(SELECT_BASE + "WHERE t.id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    /** Backward-compatible convenience wrapper. */
    public Trip findByIdDirect(int id) { return findById(id).orElse(null); }

    @Override
    public boolean create(Trip trip) {
        String sql = "INSERT INTO trips (driver_id, car_id, departure, arrival, departure_time, start_date, end_date, repeat_days, available_seats, price, luggage_policy, music_preference, smoking_allowed, ac_available, flexible_pickup, women_only, pets_allowed, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, trip.getDriverId());
            ps.setInt(2, trip.getCarId());
            ps.setString(3, trip.getDeparture());
            ps.setString(4, trip.getArrival());
            ps.setTime(5, Time.valueOf(trip.getDepartureTime()));
            ps.setDate(6, Date.valueOf(trip.getStartDate()));
            ps.setObject(7, trip.getEndDate() != null ? Date.valueOf(trip.getEndDate()) : null);
            ps.setString(8, trip.getRepeatDaysString());
            ps.setInt(9, trip.getAvailableSeats());
            ps.setDouble(10, trip.getPrice());
            ps.setString(11, trip.getLuggagePolicy().name());
            ps.setString(12, trip.getMusicPreference().name());
            ps.setBoolean(13, trip.isSmokingAllowed());
            ps.setBoolean(14, trip.isAcAvailable());
            ps.setBoolean(15, trip.isFlexiblePickup());
            ps.setBoolean(16, trip.isWomenOnly());
            ps.setBoolean(17, trip.isPetsAllowed());
            ps.setString(18, trip.getStatus().name());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) trip.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public List<Trip> findAll() { return findAllActive(); }

    @Override
    public boolean update(Trip trip) { return false; } // Updates done via specific methods below

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Trip> findAllActive() {
        List<Trip> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE t.status='ACTIVE' ORDER BY t.start_date ASC LIMIT 50";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Trip> findByDriver(int driverId) {
        List<Trip> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(SELECT_BASE + "WHERE t.driver_id=? ORDER BY t.start_date DESC")) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Trip> search(String departure, String arrival, LocalDate date, List<com.covoitdark.dao.filters.TripFilter> filters) {
        List<Trip> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE t.status='ACTIVE' AND t.available_seats > 0 ");
        sql.append("AND t.departure LIKE ? AND t.arrival LIKE ? AND t.start_date >= ? ");
        
        if (filters != null) {
            for (com.covoitdark.dao.filters.TripFilter filter : filters) {
                filter.apply(sql);
            }
        }

        sql.append("ORDER BY t.start_date ASC, t.departure_time ASC");
        
        try (PreparedStatement ps = conn().prepareStatement(sql.toString())) {
            ps.setString(1, "%" + departure + "%");
            ps.setString(2, "%" + arrival + "%");
            ps.setDate(3, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    /** Backward compatibility wrapper */
    public List<Trip> search(String departure, String arrival, LocalDate date) {
        return search(departure, arrival, date, new ArrayList<>());
    }

    // ── Stream-based filters ─────────────────────────────────────────────────

    /** Stream: filter active trips by minimum available seats. */
    public List<Trip> findWithMinSeats(int minSeats) {
        return findAllActive().stream()
                .filter(t -> t.getAvailableSeats() >= minSeats)
                .collect(Collectors.toList());
    }

    /** Stream: find cheapest trips up to a max price, sorted ascending. */
    public List<Trip> findByMaxPrice(double maxPrice) {
        return findAllActive().stream()
                .filter(t -> t.getPrice() <= maxPrice)
                .sorted(Comparator.comparingDouble(Trip::getPrice))
                .collect(Collectors.toList());
    }

    /**
     * Stream + map: extract the set of unique departure cities from all active trips.
     * Demonstrates Stream.map() — transforms Trip objects to String departure names.
     */
    public List<String> findActiveDepartureCities() {
        return findAllActive().stream()
                .map(Trip::getDeparture)          // map: Trip → String
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ── Specific Update Methods ───────────────────────────────────────────────

    public boolean updateStatus(int tripId, Trip.Status status) {
        String sql = "UPDATE trips SET status=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean decrementSeats(int tripId) {
        String sql = "UPDATE trips SET available_seats = available_seats - 1 WHERE id=? AND available_seats > 0";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean incrementSeats(int tripId) {
        String sql = "UPDATE trips SET available_seats = available_seats + 1 WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int countAcceptedPassengers(int tripId) {
        String sql = "SELECT COUNT(*) FROM requests WHERE trip_id=? AND status='ACCEPTED'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Row Mapping ──────────────────────────────────────────────────────────

    private Trip mapRow(ResultSet rs) throws SQLException {
        Trip t = new Trip();
        t.setId(rs.getInt("id"));
        t.setDriverId(rs.getInt("driver_id"));
        t.setCarId(rs.getInt("car_id"));
        t.setDeparture(rs.getString("departure"));
        t.setArrival(rs.getString("arrival"));
        t.setDepartureTime(rs.getTime("departure_time").toLocalTime());
        t.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) t.setEndDate(endDate.toLocalDate());
        String repeatDays = rs.getString("repeat_days");
        if (repeatDays != null && !repeatDays.isBlank())
            t.setRepeatDays(Arrays.asList(repeatDays.split(",")));
        t.setAvailableSeats(rs.getInt("available_seats"));
        t.setPrice(rs.getDouble("price"));
        t.setLuggagePolicy(Trip.LuggagePolicy.valueOf(rs.getString("luggage_policy")));
        t.setMusicPreference(Trip.MusicPreference.valueOf(rs.getString("music_preference")));
        t.setSmokingAllowed(rs.getBoolean("smoking_allowed"));
        t.setAcAvailable(rs.getBoolean("ac_available"));
        t.setFlexiblePickup(rs.getBoolean("flexible_pickup"));
        t.setWomenOnly(rs.getBoolean("women_only"));
        t.setPetsAllowed(rs.getBoolean("pets_allowed"));
        t.setStatus(Trip.Status.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        try { t.setDriverName(rs.getString("driver_name")); }    catch (SQLException ignored) {}
        try { t.setDriverRating(rs.getDouble("driver_rating")); } catch (SQLException ignored) {}
        try { t.setCarInfo(rs.getString("car_info")); }           catch (SQLException ignored) {}
        try { t.setDriverAvatar(rs.getString("driver_avatar")); } catch (SQLException ignored) {}
        try { t.setCarImage(rs.getString("car_image")); }         catch (SQLException ignored) {}
        try { t.setCarColor(rs.getString("car_color")); }         catch (SQLException ignored) {}
        try { t.setCarPlate(rs.getString("car_plate")); }         catch (SQLException ignored) {}
        try { t.setTotalSeats(rs.getInt("total_seats")); }        catch (SQLException ignored) {}
        return t;
    }
}
