package org.dimasik.flameReports.database.impl;

import org.dimasik.flameReports.database.DatabaseManager;
import org.dimasik.flameReports.database.tables.ReportTable;
import org.dimasik.flameReports.models.Report;

import java.sql.*;

import java.util.concurrent.CompletableFuture;

public class SqlReportTable implements ReportTable {

    private final DatabaseManager db;

    public SqlReportTable(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Integer> createReport(String suspectName, String reporterName, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO reports (suspect_name, reporter_name, reason) VALUES (?, ?, ?)";
            try (Connection conn = db.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, suspectName);
                stmt.setString(2, reporterName);
                stmt.setString(3, reason);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        return rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Report> getReport(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM reports WHERE id = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Report(
                                rs.getInt("id"), rs.getString("suspect_name"),
                                rs.getString("reporter_name"), rs.getString("reason"),
                                rs.getTimestamp("created_at"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Boolean> hasReport(String sender, String target) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM reports WHERE reporter_name = ? AND suspect_name = ? LIMIT 1";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, sender);
                stmt.setString(2, target);
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
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS reports (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "suspect_name VARCHAR(16) NOT NULL, " +
                "reporter_name VARCHAR(16) NOT NULL, " +
                "reason VARCHAR(150) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_suspect (suspect_name), " +
                "INDEX idx_reporter (reporter_name), " +
                "INDEX idx_created (created_at))";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}