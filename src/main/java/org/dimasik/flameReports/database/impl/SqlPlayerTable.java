package org.dimasik.flameReports.database.impl;

import org.dimasik.flameReports.database.DatabaseManager;
import org.dimasik.flameReports.database.tables.PlayerTable;
import org.dimasik.flameReports.models.Player;

import java.sql.*;

import java.util.concurrent.CompletableFuture;

public class SqlPlayerTable implements PlayerTable {

    private final DatabaseManager db;

    public SqlPlayerTable(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Void> setOrCreateServer(String nickname, String server) {
        return CompletableFuture.runAsync(() -> {
            boolean exists = false;
            String checkSql = "SELECT id FROM players WHERE nickname = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setString(1, nickname);
                try (ResultSet rs = stmt.executeQuery()) {
                    exists = rs.next();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            if (exists) {
                String updateSql = "UPDATE players SET server = ? WHERE nickname = ?";
                try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, server);
                    stmt.setString(2, nickname);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                String insertSql = "INSERT INTO players (nickname, server) VALUES (?, ?)";
                try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setString(1, nickname);
                    stmt.setString(2, server);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<String> getPlayerServer(String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT server FROM players WHERE nickname = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nickname);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return rs.getString("server");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Boolean> hasPlayer(String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM players WHERE nickname = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nickname);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Player> getPlayer(String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM players WHERE nickname = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nickname);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return mapPlayer(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Player> getPlayerById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM players WHERE id = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return mapPlayer(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Player> getOrCreatePlayer(String nickname) {
        return getPlayer(nickname).thenCompose(p -> {
            if (p != null)
                return CompletableFuture.completedFuture(p);

            return CompletableFuture.supplyAsync(() -> {
                String insert = "INSERT INTO players (nickname) VALUES (?)";
                try (Connection conn = db.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, nickname);
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            Player newPlayer = new Player();
                            newPlayer.setId(rs.getInt(1));
                            newPlayer.setNickname(nickname);
                            return newPlayer;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }, db.getExecutor());
        });
    }

    @Override
    public CompletableFuture<Void> incrementCorrectReports(int playerId) {
        return updateStat(playerId, "correct_reports");
    }

    @Override
    public CompletableFuture<Void> incrementIncorrectReports(int playerId) {
        return updateStat(playerId, "incorrect_reports");
    }

    private CompletableFuture<Void> updateStat(int playerId, String column) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE players SET " + column + " = " + column + " + 1 WHERE id = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, playerId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, db.getExecutor());
    }

    private Player mapPlayer(ResultSet rs) throws SQLException {
        return new Player(
                rs.getInt("id"), rs.getString("nickname"),
                rs.getInt("correct_reports"), rs.getInt("incorrect_reports"),
                rs.getString("server"), rs.getTimestamp("created_at"));
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nickname VARCHAR(16) NOT NULL UNIQUE, " +
                "correct_reports INT DEFAULT 0, " +
                "incorrect_reports INT DEFAULT 0, " +
                "server VARCHAR(32) NULL DEFAULT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_nickname (nickname), " +
                "INDEX idx_server (server))";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}