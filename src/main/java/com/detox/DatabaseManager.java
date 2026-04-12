package com.detox;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the SQLite database connection and all CRUD operations.
 * The database file (detox.db) is created in the user's home directory.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:" +
        System.getProperty("user.home") + "/detox.db";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        connection.createStatement().execute("PRAGMA foreign_keys = ON;");
        initializeTables();
    }

    /** Singleton accessor */
    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Schema Setup
    // ──────────────────────────────────────────────────────────────────────────

    private void initializeTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    username        TEXT    UNIQUE NOT NULL,
                    password_hash   TEXT    NOT NULL,
                    name            TEXT    NOT NULL,
                    daily_safe_limit  INTEGER NOT NULL DEFAULT 120,
                    sleep_start_hour  INTEGER NOT NULL DEFAULT 22,
                    sleep_end_hour    INTEGER NOT NULL DEFAULT 6
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS records (
                    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id             INTEGER NOT NULL,
                    record_date         TEXT    NOT NULL,
                    study_time          INTEGER NOT NULL DEFAULT 0,
                    social_time         INTEGER NOT NULL DEFAULT 0,
                    entertainment_time  INTEGER NOT NULL DEFAULT 0,
                    total_time          INTEGER NOT NULL DEFAULT 0,
                    peak_usage_hour     INTEGER NOT NULL DEFAULT 12,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  User Operations
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new user. Password is hashed with BCrypt.
     * @return the saved User with its generated ID, or null on failure.
     */
    public User registerUser(String username, String plainPassword, String name,
                             int safeLimit, int sleepStart, int sleepEnd) throws SQLException {
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        String sql = "INSERT INTO users(username, password_hash, name, daily_safe_limit, sleep_start_hour, sleep_end_hour) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, name);
            ps.setInt(4, safeLimit);
            ps.setInt(5, sleepStart);
            ps.setInt(6, sleepEnd);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return findUserById(keys.getLong(1));
            }
        }
        return null;
    }

    /**
     * Attempts to authenticate a user.
     * @return the User if credentials match, null otherwise.
     */
    public User loginUser(String username, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (BCrypt.checkpw(plainPassword, storedHash)) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User findUserById(long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        }
        return null;
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Screen Time Record Operations
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Saves a new screen time record for today. Replaces if one already exists for today.
     */
    public void saveRecord(ScreenTimeRecord record) throws SQLException {
        // Delete today's record if it exists (upsert behaviour)
        String del = "DELETE FROM records WHERE user_id = ? AND record_date = ?";
        try (PreparedStatement ps = connection.prepareStatement(del)) {
            ps.setLong(1, record.getUserId());
            ps.setString(2, record.getDate().toString());
            ps.executeUpdate();
        }

        String sql = """
            INSERT INTO records(user_id, record_date, study_time, social_time,
                                entertainment_time, total_time, peak_usage_hour)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, record.getUserId());
            ps.setString(2, record.getDate().toString());
            ps.setInt(3, record.getStudyTime());
            ps.setInt(4, record.getSocialTime());
            ps.setInt(5, record.getEntertainmentTime());
            ps.setInt(6, record.getTotalTime());
            ps.setInt(7, record.getPeakUsageHour());
            ps.executeUpdate();
        }
    }

    /** Returns the last N records for a user, ordered by date DESC. */
    public List<ScreenTimeRecord> getRecentRecords(long userId, int limit) throws SQLException {
        String sql = "SELECT * FROM records WHERE user_id = ? ORDER BY record_date DESC LIMIT ?";
        List<ScreenTimeRecord> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRecord(rs));
        }
        return list;
    }

    /** Returns today's record for a user, or null if not logged yet. */
    public ScreenTimeRecord getTodayRecord(long userId) throws SQLException {
        String sql = "SELECT * FROM records WHERE user_id = ? AND record_date = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, LocalDate.now().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRecord(rs);
        }
        return null;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Mappers
    // ──────────────────────────────────────────────────────────────────────────

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password_hash"));
        u.setName(rs.getString("name"));
        u.setDailySafeLimit(rs.getInt("daily_safe_limit"));
        u.setSleepStartHour(rs.getInt("sleep_start_hour"));
        u.setSleepEndHour(rs.getInt("sleep_end_hour"));
        return u;
    }

    private ScreenTimeRecord mapRecord(ResultSet rs) throws SQLException {
        ScreenTimeRecord r = new ScreenTimeRecord();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setDate(LocalDate.parse(rs.getString("record_date")));
        r.setStudyTime(rs.getInt("study_time"));
        r.setSocialTime(rs.getInt("social_time"));
        r.setEntertainmentTime(rs.getInt("entertainment_time"));
        r.setTotalTime(rs.getInt("total_time"));
        r.setPeakUsageHour(rs.getInt("peak_usage_hour"));
        return r;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
