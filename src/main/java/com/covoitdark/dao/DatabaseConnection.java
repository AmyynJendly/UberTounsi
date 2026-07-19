package com.covoitdark.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager.
 */
public class DatabaseConnection {

    private static final String DB_HOST  = System.getenv().getOrDefault("DB_HOST", "localhost");
    private static final String DB_PORT  = System.getenv().getOrDefault("DB_PORT", "3306");
    private static final String DB_NAME  = System.getenv().getOrDefault("DB_NAME", "covoiturage");
    private static final String URL      = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8", DB_HOST, DB_PORT, DB_NAME);
    private static final String USER     = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    private static DatabaseConnection instance;
    private Connection connection;

    private static final int    MAX_RETRIES    = 15;
    private static final long   RETRY_DELAY_MS = 2000;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found on classpath.", e);
        }

        SQLException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to database on attempt " + attempt);
                return;
            } catch (SQLException e) {
                lastException = e;
                System.err.println("[DB] Attempt " + attempt + "/" + MAX_RETRIES +
                        " failed: " + e.getMessage() + ". Retrying in " + (RETRY_DELAY_MS / 1000) + "s...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw new RuntimeException("Failed to connect to database after " + MAX_RETRIES +
                " attempts: " + (lastException != null ? lastException.getMessage() : "unknown"), lastException);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private static boolean isConnectionClosed() {
        try {
            return instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    public Connection getConnection() {
        if (isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
