package org.dimasik.flameReports.database.tables;

import org.dimasik.flameReports.models.ActiveMute;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnusedReturnValue")
public interface ActionsTable {
    CompletableFuture<Void> setAction(String player, String action);
    CompletableFuture<String> getAction(String player);
    CompletableFuture<Boolean> removeAction(String player);
    CompletableFuture<Boolean> actionPlayerExists(String player);
    void createTable();
}
