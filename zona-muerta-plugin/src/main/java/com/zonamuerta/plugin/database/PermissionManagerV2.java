package com.zonamuerta.plugin.database;

import java.sql.*;
import java.util.UUID;

public class PermissionManagerV2 {
    private final DatabaseManager db;

    public PermissionManagerV2(DatabaseManager db) {
        this.db = db;
    }

    public void setRank(UUID uuid, String rank) {
        String sql = "INSERT OR REPLACE INTO permissions (uuid, rank) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, rank.toUpperCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getRank(UUID uuid) {
        String sql = "SELECT rank FROM permissions WHERE uuid = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("rank");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "MEMBER";
    }
}