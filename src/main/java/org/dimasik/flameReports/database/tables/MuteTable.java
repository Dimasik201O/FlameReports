package org.dimasik.flameReports.database.tables;

import java.util.concurrent.CompletableFuture;

import org.dimasik.flameReports.models.ActiveMute;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnusedReturnValue")
public interface MuteTable {
    CompletableFuture<Void> addMute(String playerName, String reason, Long until);
    CompletableFuture<ActiveMute> checkMute(String playerName);
    CompletableFuture<Void> removeMute(String playerName);
    void createTable();
    long parseTime(String period);
    String parseTime(@Nullable Long seconds);
}
