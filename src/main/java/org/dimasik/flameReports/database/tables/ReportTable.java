package org.dimasik.flameReports.database.tables;

import org.dimasik.flameReports.models.Report;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnusedReturnValue")
public interface ReportTable {
    CompletableFuture<Integer> createReport(String suspectName, String reporterName, String reason);
    CompletableFuture<Report> getReport(int id);
    CompletableFuture<Boolean> hasReport(String sender, String target);
    void createTable();
}