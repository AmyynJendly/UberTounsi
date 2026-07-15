package com.covoitdark.dao;

import com.covoitdark.models.User;
import com.covoitdark.models.Passager;
import com.covoitdark.models.Chauffeur;
import com.covoitdark.models.Admin;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserDAO implements IRepository<User, Integer> {

    // ConcurrentHashMap cache — thread-safe for the multi-threaded HTTP server
    private final Map<Integer, User> cache = new ConcurrentHashMap<>();


    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }


    public User findByEmailOrPhone(String identifier) {
        String sql = "SELECT * FROM users WHERE email = ? OR phone = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** IRepository: find by primary key, returning Optional to eliminate null checks. */
    @Override
    public Optional<User> findById(int id) {
        // Check the Map cache first
        if (cache.containsKey(id)) return Optional.of(cache.get(id));
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapRow(rs);
                cache.put(id, u); // store in Map cache
                return Optional.of(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    /** Convenience non-Optional version for backward compatibility with existing code. */
    public User findByIdDirect(int id) {
        return findById(id).orElse(null);
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    /** Stream: filter to only drivers from the full user list. */
    public List<User> findAllDrivers() {
        return findAll().stream()
                .filter(u -> u.getRole() == User.Role.DRIVER)
                .sorted(Comparator.comparing(User::getFullName))
                .collect(Collectors.toList());
    }

    /** Stream: get top-rated users (reputation >= threshold). */
    public List<User> findTopRated(double minScore) {
        return findAll().stream()
                .filter(u -> u.getReputationScore() >= minScore)
                .sorted(Comparator.comparingDouble(User::getReputationScore).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean create(User user) {
        String sql = "INSERT INTO users (full_name, email, password, salt, phone, role, bio, languages, reputation_score, is_verified, is_blocked, balance, avatar) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.setString(7, user.getBio());
            ps.setString(8, user.getLanguagesString());
            ps.setDouble(9, user.getReputationScore());
            ps.setBoolean(10, user.isVerified());
            ps.setBoolean(11, user.isBlocked());
            ps.setDouble(12, user.getBalance());
            ps.setString(13, user.getAvatar());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) user.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean update(User user) {
        cache.remove(user.getId()); // Invalidate cache on update
        String sql = "UPDATE users SET full_name=?, email=?, phone=?, bio=?, languages=?, reputation_score=?, avatar=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getBio());
            ps.setString(5, user.getLanguagesString());
            ps.setDouble(6, user.getReputationScore());
            ps.setString(7, user.getAvatar());
            ps.setInt(8, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updatePassword(int userId, String newHash, String newSalt) {
        String sql = "UPDATE users SET password=?, salt=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, newSalt);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean setBlocked(int userId, boolean blocked) {
        String sql = "UPDATE users SET is_blocked=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBoolean(1, blocked);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean incrementFailedLogins(int userId) {
        String sql = "UPDATE users SET failed_logins = failed_logins + 1 WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean resetFailedLogins(int userId) {
        String sql = "UPDATE users SET failed_logins = 0 WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateBalance(int userId, double balance) {
        String sql = "UPDATE users SET balance=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, balance);
            ps.setInt(2, userId);
            boolean success = ps.executeUpdate() > 0;
            if (success) cache.remove(userId);
            return success;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        cache.remove(id);
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateReputationScore(int userId, double score) {
        String sql = "UPDATE users SET reputation_score=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, score);
            ps.setInt(2, userId);
            boolean success = ps.executeUpdate() > 0;
            if (success) cache.remove(userId);
            return success;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean setVerified(int userId, boolean verified) {
        cache.remove(userId);
        String sql = "UPDATE users SET is_verified=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBoolean(1, verified);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean uploadIdDocument(int userId, String base64Image) {
        cache.remove(userId);
        String sql = "UPDATE users SET id_document=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, base64Image);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public String getIdDocument(int userId) {
        String sql = "SELECT id_document FROM users WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("id_document");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean setIdVerified(int userId, boolean verified) {
        cache.remove(userId);
        String sql = "UPDATE users SET id_verified=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBoolean(1, verified);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateCancellationRate(int userId, double rate) {
        cache.remove(userId);
        String sql = "UPDATE users SET cancellation_rate=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, rate);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User.Role role = User.Role.valueOf(rs.getString("role"));
        User u = switch (role) {
            case DRIVER -> new Chauffeur();
            case ADMIN -> new Admin();
            default -> new Passager();
        };
        
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setSalt(rs.getString("salt"));
        u.setPhone(rs.getString("phone"));
        u.setRole(role);
        u.setBio(rs.getString("bio"));
        String langs = rs.getString("languages");
        if (langs != null && !langs.isBlank()) {
            u.setLanguages(Arrays.asList(langs.split(",")));
        }
        u.setReputationScore(rs.getDouble("reputation_score"));
        u.setVerified(rs.getBoolean("is_verified"));
        u.setBlocked(rs.getBoolean("is_blocked"));
        u.setBalance(rs.getDouble("balance"));
        u.setAvatar(rs.getString("avatar"));
        u.setFailedLogins(rs.getInt("failed_logins"));
        try { u.setIdVerified(rs.getBoolean("id_verified")); } catch (SQLException ignored) {}
        try { u.setCancellationRate(rs.getDouble("cancellation_rate")); } catch (SQLException ignored) {}
        try { u.setIdDocument(rs.getString("id_document")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
    public List<User> findRelevantContacts(int userId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT DISTINCT u.* FROM users u " +
                     "WHERE u.role = 'ADMIN' " +
                     "OR u.id IN (" +
                     "  SELECT t.driver_id FROM trips t " +
                     "  JOIN requests r ON t.id = r.trip_id " +
                     "  WHERE r.passenger_id = ? AND r.status = 'ACCEPTED'" +
                     ") " +
                     "OR u.id IN (" +
                     "  SELECT r.passenger_id FROM requests r " +
                     "  JOIN trips t ON r.trip_id = t.id " +
                     "  WHERE t.driver_id = ? AND r.status = 'ACCEPTED'" +
                     ") " +
                     "ORDER BY u.full_name ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }
}
