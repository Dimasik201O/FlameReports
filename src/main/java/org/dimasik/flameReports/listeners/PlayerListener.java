package org.dimasik.flameReports.listeners;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.configuration.ConfigManager;
import org.dimasik.flameReports.database.DatabaseManager;
import org.dimasik.flameReports.utils.Parser;

import java.util.List;

public class PlayerListener implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String playerName = player.getName();
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
        DatabaseManager databaseManager = FlameReports.getInstance().getDatabaseManager();
        databaseManager.getActionsTable().actionPlayerExists(playerName).thenAccept(exists -> {
            if(exists){
                String action = databaseManager.getActionsTable().getAction(playerName).join();
                databaseManager.getActionsTable().removeAction(playerName);
                String prefix = action.split(" ")[0];
                switch (prefix){
                    case "spectate" -> {
                        String[] split = action.split(" ", 2);
                        String target = split[1];
                        Bukkit.getScheduler().runTask(FlameReports.getInstance(), () -> {
                            Player teleportTo = Bukkit.getPlayerExact(target);
                            if(teleportTo == null) return;
                            if(Bukkit.getPluginManager().isPluginEnabled("Essentials")){
                                Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
                                assert essentials != null;
                                User user = essentials.getUser(player);
                                user.setVanished(true);
                            }
                            player.setGameMode(GameMode.SPECTATOR);
                            player.teleport(teleportTo);
                            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        });
                    }
                }
            }
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
