package org.dimasik.flameReports.database.tables;

import org.dimasik.flameReports.models.Player;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnusedReturnValue")
public interface PlayerTable {
    CompletableFuture<Void> setOrCreateServer(String nickname, String server);
    CompletableFuture<String> getPlayerServer(String nickname);
    CompletableFuture<Boolean> hasPlayer(String nickname);
    CompletableFuture<Player> getPlayer(String nickname);
    CompletableFuture<Player> getPlayerById(int id);
    CompletableFuture<Player> getOrCreatePlayer(String nickname);
    CompletableFuture<Void> incrementCorrectReports(int playerId);
    CompletableFuture<Void> incrementIncorrectReports(int playerId);
    void createTable();
}