package org.dimasik.flameReports.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.configuration.ConfigManager;
import org.dimasik.flameReports.utils.Parser;

import java.util.List;

public class PlayerListener implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event){
        Player player = event.getPlayer();
        FlameReports.getInstance().getDatabaseManager().getPlayers().setOrCreateServer(
                player.getName(),
                ConfigManager.getString("config.yml", "server.mode", "unknown")
        ).thenAccept(av -> FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlockByModerator(player.getName()).thenAccept(rb -> {
            if(rb == null) return;
            FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayerById(rb.getPlayerId()).thenAccept(target -> {
                player.sendMessage(Parser.color("&#00D5FC▶ &fДело игрока &#00D5FC" + target.getNickname() + (target.getServer() != null ? " &8(" + target.getServer() + ")" : "") + "&f на рассмотрении."));
                Parser.sendCopyableMessage(player, List.of("             &#00D5FC[Скопировать никнейм]"), target.getNickname(), "Нажми, чтобы скопировать");
            });
        }));
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
