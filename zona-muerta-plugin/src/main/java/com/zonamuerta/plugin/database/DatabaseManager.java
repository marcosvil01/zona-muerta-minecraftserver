package com.zonamuerta.plugin.database;

import com.zonamuerta.plugin.ZonaMuerta;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager(ZonaMuerta plugin) {
        try {
            File dataFolder = new File(plugin.getDataFolder(), "database.db");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            createTables();
        } catch (Exception e) {
            plugin.getLogger().severe("Error initializing database: " + e.getMessage());
        }
    }

    private void createTables() {
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS permissions (uuid TEXT PRIMARY KEY, rank TEXT)")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS player_data (uuid TEXT PRIMARY KEY, data TEXT)")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}