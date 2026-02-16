package org.dimasik.flameReports.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.dimasik.flameReports.database.impl.*;
import org.dimasik.flameReports.database.tables.*;

import java.sql.Connection;
import java.sql.SQLException;

@Getter
public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final PlayerTable players;
    private final ReportTable reports;
    private final ReportBlockTable reportBlocks;
    private final MuteTable mutes;
    private final ActionsTable actionsTable;
    private final java.util.concurrent.ExecutorService executor;

    public DatabaseManager(String host, String user, String password, String database) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + "/" + database + "?useSSL=false&autoReconnect=true");
        config.setUsername(user);
        config.setPassword(password);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
        this.executor = java.util.concurrent.Executors.newCachedThreadPool();

        this.players = new SqlPlayerTable(this);
        this.reports = new SqlReportTable(this);
        this.reportBlocks = new SqlReportBlockTable(this);
        this.mutes = new SqlMuteTable(this);
        this.actionsTable = new SqlActionsTable(this);

        initTables();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void initTables() {
        this.players.createTable();
        this.reports.createTable();
        this.reportBlocks.createTable();
        this.mutes.createTable();
        this.actionsTable.createTable();
    }

    public java.util.concurrent.CompletableFuture<Integer> executeAddReportTransaction(String suspectName,
            String reporterName, String reason) {
        return reports.createReport(suspectName, reporterName, reason).thenCompose(reportId -> {
            if (reportId == -1)
                return java.util.concurrent.CompletableFuture.completedFuture(null);

            return players.getOrCreatePlayer(suspectName).thenCompose(suspect -> {
                if (suspect == null)
                    return java.util.concurrent.CompletableFuture.completedFuture(reportId);

                reportBlocks.addReportToBlock(suspect.getId(), reportId);
                return java.util.concurrent.CompletableFuture.completedFuture(reportId);
            });
        });
    }
}