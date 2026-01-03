package org.dimasik.flameReports.database.impl;

import org.dimasik.flameReports.database.DatabaseManager;
import org.dimasik.flameReports.database.tables.ReportBlockTable;
import org.dimasik.flameReports.enums.EnumReportStatus;
import org.dimasik.flameReports.enums.EnumSortingType;
import org.dimasik.flameReports.models.ReportBlock;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.util.concurrent.CompletableFuture;

public class SqlReportBlockTable implements ReportBlockTable {

    private final DatabaseManager db;

    public SqlReportBlockTable(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Void> addReportToBlock(int playerId, int reportId) {
        return CompletableFuture.runAsync(() -> {
            String findBlock = "SELECT id, report_ids FROM report_blocks WHERE player = ? AND status = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(findBlock)) {
                stmt.setInt(1, playerId);
                stmt.setString(2, EnumReportStatus.WAITING.name());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int blockId = rs.getInt("id");
                        String currentIds = rs.getString("report_ids");
                        String newIds = currentIds + "," + reportId;
                        updateBlockIds(blockId, newIds);
                    } else {
                        createBlock(playerId, reportId);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, db.getExecutor());
    }

    private void updateBlockIds(int blockId, String newIds) {
        String sql = "UPDATE report_blocks SET report_ids = ? WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newIds);
            stmt.setInt(2, blockId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createBlock(int playerId, int reportId) {
        String sql = "INSERT INTO report_blocks (player, report_ids, status) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setString(2, String.valueOf(reportId));
            stmt.setString(3, EnumReportStatus.WAITING.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<List<ReportBlock>> getReportBlocks(String viewerName, int page, int size,
            EnumSortingType sort,
            boolean onlineOnly) {
        return CompletableFuture.supplyAsync(() -> {
            List<ReportBlock> blocks = new ArrayList<>();
            int offset = (page - 1) * size;

            StringBuilder sql = new StringBuilder(
                    "SELECT rb.*, " +
                            "(LENGTH(rb.report_ids) - LENGTH(REPLACE(rb.report_ids, ',', '')) + 1) as report_count, " +
                            "(SELECT MAX(CASE WHEN rp.incorrect_reports = 0 THEN 1 ELSE rp.correct_reports / rp.incorrect_reports END) "
                            +
                            " FROM reports r " +
                            " JOIN players rp ON r.reporter_name = rp.nickname " +
                            " WHERE FIND_IN_SET(r.id, rb.report_ids)) as max_decency " +
                            "FROM report_blocks rb " +
                            "JOIN players p ON rb.player = p.id " +
                            "WHERE rb.moderator IS NULL AND rb.status = ? ");

            if (onlineOnly)
                sql.append("AND p.server IS NOT NULL ");
            else
                sql.append("AND p.server IS NULL ");

            switch (sort) {
                case NEWEST_FIRST:
                    sql.append("ORDER BY rb.created_at DESC ");
                    break;
                case OLDEST_FIRST:
                    sql.append("ORDER BY rb.created_at ASC ");
                    break;
                case AMOUNT_FIRST:
                    sql.append("ORDER BY report_count DESC ");
                    break;
                case DECENCY_FIRST:
                    sql.append("ORDER BY max_decency DESC ");
                    break;
            }

            sql.append("LIMIT ? OFFSET ?");

            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                stmt.setString(1, EnumReportStatus.WAITING.name());
                stmt.setInt(2, size);
                stmt.setInt(3, offset);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        blocks.add(mapReportBlock(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return blocks;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Integer> getReportPageCount(String viewerName, int pageSize, boolean onlineOnly) {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder sql = new StringBuilder(
                    "SELECT COUNT(*) FROM report_blocks rb " +
                            "JOIN players p ON rb.player = p.id " +
                            "WHERE rb.moderator IS NULL AND rb.status = ? ");

            if (onlineOnly)
                sql.append("AND p.server IS NOT NULL ");
            else
                sql.append("AND p.server IS NULL ");

            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                stmt.setString(1, EnumReportStatus.WAITING.name());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt(1);
                        if (total == 0)
                            return 0;
                        return (total + pageSize - 1) / pageSize;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Void> setModerator(int blockId, String moderator) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE report_blocks SET moderator = ? WHERE id = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, moderator);
                stmt.setInt(2, blockId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Void> setStatus(int blockId, EnumReportStatus status) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE report_blocks SET status = ? WHERE id = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.name());
                stmt.setInt(2, blockId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<Void> deleteReportBlock(int blockId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection()) {
                String getIds = "SELECT report_ids FROM report_blocks WHERE id = ?";
                String reportIds = null;
                try (PreparedStatement stmt = conn.prepareStatement(getIds)) {
                    stmt.setInt(1, blockId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next())
                            reportIds = rs.getString("report_ids");
                    }
                }

                if (reportIds != null && !reportIds.isEmpty()) {
                    String safeIds = Arrays.stream(reportIds.split(","))
                            .map(String::trim)
                            .filter(s -> s.matches("\\d+"))
                            .collect(Collectors.joining(","));

                    if (!safeIds.isEmpty()) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("DELETE FROM reports WHERE id IN (" + safeIds + ")");
                        }
                    }
                }

                String deleteBlock = "DELETE FROM report_blocks WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteBlock)) {
                    stmt.setInt(1, blockId);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<ReportBlock> getReportBlock(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM report_blocks WHERE id = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return mapReportBlock(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, db.getExecutor());
    }

    @Override
    public CompletableFuture<ReportBlock> getReportBlock(String moderator) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM report_blocks WHERE moderator = ? AND status = ?";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, moderator);
                stmt.setString(2, EnumReportStatus.WAITING.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return mapReportBlock(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, db.getExecutor());
    }

    private ReportBlock mapReportBlock(ResultSet rs) throws SQLException {
        return new ReportBlock(
                rs.getInt("id"),
                rs.getInt("player"),
                rs.getString("report_ids"),
                rs.getString("moderator"),
                rs.getTimestamp("created_at"),
                EnumReportStatus.valueOf(rs.getString("status")));
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS report_blocks (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "player INT NOT NULL, " +
                "report_ids TEXT NOT NULL, " +
                "moderator VARCHAR(16) NULL DEFAULT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status ENUM('WAITING', 'ACCEPTED', 'DECLINED') DEFAULT 'WAITING', " +
                "INDEX idx_player (player), " +
                "INDEX idx_moderator (moderator))";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}