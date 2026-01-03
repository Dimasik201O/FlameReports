package org.dimasik.flameReports.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.configuration.ConfigManager;

public class PlayerListener implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event){
        Player player = event.getPlayer();
        FlameReports.getInstance().getDatabaseManager().getPlayers().setOrCreateServer(
                player.getName(),
                ConfigManager.getString("config.yml", "server.mode", "unknown")
        );
        FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlockByPlayer(player.getName()).thenAccept(rb -> {
            if(rb == null) return;
            if(rb.getReportIds().isEmpty()) return;
            if(rb.getModerator() == null)
                FlameReports.getInstance().getRedisManager().publishMessage("join " + player.getName() + " " + ConfigManager.getString("config.yml", "server.mode", "unknown"));
        });
    }

    @EventHandler
    public void on(PlayerQuitEvent event){
        Player player = event.getPlayer();
        FlameReports.getInstance().getDatabaseManager().getPlayers().setOrCreateServer(
                player.getName(), null
        );
    }

    @EventHandler
    public void on(PlayerKickEvent event){
        Player player = event.getPlayer();
        FlameReports.getInstance().getDatabaseManager().getPlayers().setOrCreateServer(
                player.getName(), null
        );
    }
}
