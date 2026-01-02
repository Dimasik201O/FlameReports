package org.dimasik.flameReports.database.tables;

import org.dimasik.flameReports.enums.EnumReportStatus;
import org.dimasik.flameReports.enums.EnumSortingType;
import org.dimasik.flameReports.models.ReportBlock;
import java.util.List;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnusedReturnValue")
public interface ReportBlockTable {
    CompletableFuture<Void> addReportToBlock(int playerId, int reportId);
    CompletableFuture<List<ReportBlock>> getReportBlocks(String viewerName, int page, int size, EnumSortingType sort,
            boolean onlineOnly);
    CompletableFuture<Integer> getReportPageCount(String viewerName, int pageSize, boolean onlineOnly);
    CompletableFuture<Void> setModerator(int blockId, String moderator);
    CompletableFuture<Void> setStatus(int blockId, EnumReportStatus status);
    CompletableFuture<Void> deleteReportBlock(int blockId);
    CompletableFuture<ReportBlock> getReportBlock(int id);
    CompletableFuture<ReportBlock> getReportBlock(String moderator);
    void createTable();
}