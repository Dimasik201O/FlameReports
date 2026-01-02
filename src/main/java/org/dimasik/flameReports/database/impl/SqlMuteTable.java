package org.dimasik.flameReports.database.impl;

import org.dimasik.flameReports.database.DatabaseManager;
import org.dimasik.flameReports.database.tables.MuteTable;
import org.dimasik.flameReports.models.ActiveMute;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlMuteTable implements MuteTable {

    private final DatabaseManager db;

    public SqlMuteTable(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Void> addMute(String playerName, String reason, Long until) {
        return CompletableFuture.runAsync(() -> {
            String deleteSql = "DELETE FROM mutes WHERE player = ?";
            String insertSql = "INSERT INTO mutes (player, reason, time, until) VALUES (?, ?, ?, ?)";

            try (Connection conn = db.getConnection()) {
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, playerName);
                    deleteStmt.executeUpdate();
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, playerName);
                    insertStmt.setString(2, reason);
                    insertStmt.setLong(3, System.currentTimeMillis());
                    if (until != null) {
                        insertStmt.setLong(4, until * 1000 + System.currentTimeMillis());
                    } else {
                        insertStmt.setNull(4, Types.BIGINT);
                    }
                    insertStmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<ActiveMute> checkMute(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT reason, until FROM mutes WHERE player = ? AND (until IS NULL OR until > ?)";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                long now = System.currentTimeMillis();
                stmt.setLong(2, now);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String reason = rs.getString("reason");
                        long until = rs.getLong("until");
                        long secondsLeft;
                        if (rs.wasNull()) {
                            secondsLeft = -1L;
                        } else {
                            secondsLeft = (until - now) / 1000;
                        }
                        return new ActiveMute(secondsLeft, reason);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Void> removeMute(String playerName) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM mutes WHERE player = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, db.getExecutor());
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS mutes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "player VARCHAR(16) NOT NULL, " +
                "reason VARCHAR(150), " +
                "time BIGINT NOT NULL, " +
                "until BIGINT DEFAULT NULL, " +
                "INDEX idx_player (player))";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long parseTime(String period) {
        if (period == null || period.isEmpty())
            return -1;
        long totalSeconds = 0;
        Pattern pattern = Pattern.compile("(\\d+)([a-zA-Z]+)");
        Matcher matcher = pattern.matcher(period);
        while (matcher.find()) {
            long val = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            switch (unit) {
                case "s":
                    totalSeconds += val;
                    break;
                case "m":
                    totalSeconds += val * 60;
                    break;
                case "h":
                    totalSeconds += val * 3600;
                    break;
                case "d":
                    totalSeconds += val * 86400;
                    break;
                case "w":
                    totalSeconds += val * 604800;
                    break;
                case "mo":
                case "mon":
                    totalSeconds += val * 2592000L;
                    break;
                case "y":
                    totalSeconds += val * 31536000L;
                    break;
                case "":
                    break;
                default:
                    return -1;
            }
        }
        return totalSeconds;
    }

    @Override
    public String parseTime(@Nullable Long seconds) {
        if (seconds == null || seconds < 0)
            return "навсегда";
        long s = seconds;

        long days = s / 86400;
        s %= 86400;
        long hours = s / 3600;
        s %= 3600;
        long minutes = s / 60;
        s %= 60;
        long sec = s;

        StringBuilder sb = new StringBuilder();
        if (days > 0)
            sb.append(days).append(" д. ");
        if (hours > 0)
            sb.append(hours).append(" ч. ");
        if (minutes > 0)
            sb.append(minutes).append(" м. ");
        if (sec > 0)
            sb.append(sec).append(" с.");

        return sb.toString().trim();
    }
}
