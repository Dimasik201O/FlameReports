package org.dimasik.flameReports.database.impl;

import org.dimasik.flameReports.database.DatabaseManager;
import org.dimasik.flameReports.database.tables.ActionsTable;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class SqlActionsTable implements ActionsTable {

    private final DatabaseManager db;

    public SqlActionsTable(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Void> setAction(String player, String action) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = db.getConnection()) {
                if (actionPlayerExists(player).join()) {
                    try (PreparedStatement stmt = connection.prepareStatement(
                            "UPDATE actions SET action = ? WHERE player = ?")) {
                        stmt.setString(1, action);
                        stmt.setString(2, player);
                        stmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement stmt = connection.prepareStatement(
                            "INSERT INTO actions (player, action) VALUES (?, ?)")) {
                        stmt.setString(1, player);
                        stmt.setString(2, action);
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<String> getAction(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = db.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(
                         "SELECT action FROM actions WHERE player = ?")) {
                stmt.setString(1, player);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("action");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> removeAction(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = db.getConnection()) {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM actions WHERE player = ?")) {
                    stmt.setString(1, player);
                    stmt.executeUpdate();
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> actionPlayerExists(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = db.getConnection()) {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT player FROM actions WHERE player = ?")) {
                    stmt.setString(1, player);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS actions (" +
                "player VARCHAR(17) PRIMARY KEY, " +
                "action VARCHAR(500))";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
