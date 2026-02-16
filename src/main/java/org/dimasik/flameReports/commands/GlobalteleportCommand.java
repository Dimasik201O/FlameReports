package org.dimasik.flameReports.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.handlers.ServerHandler;
import org.dimasik.flameReports.utils.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GlobalteleportCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) return true;
        if(args.length < 1){
            sender.sendMessage(Parser.color("&#00D5FC ▶ &fИспользование: &#00D5FC/" + alias + " [игрок]"));
            return true;
        }

        String target = args[0];
        Player teleportTo = Bukkit.getPlayerExact(target);
        if(teleportTo == null)
            FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(target).thenAccept(p -> {
                if(p.getServer() == null)
                    sender.sendMessage(Parser.color("&#00D5FC ▶ &fИгрок оффлайн"));
                else
                    FlameReports.getInstance().getDatabaseManager().getActionsTable().setAction(player.getName(), "spectate " + p.getNickname()).thenAccept((v) -> {
                        FlameReports.getInstance().getServerHandler().sendPlayerToServer(player, p.getServer());
                        player.sendMessage(Parser.color("&#00D5FC ▶ &fТелепортация..."));
                    });
            });
        else {
            if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
                assert essentials != null;
                User user = essentials.getUser(player);
                user.setVanished(true);
            }
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(teleportTo);
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.sendMessage(Parser.color("&#00D5FC ▶ &fТелепортация..."));
        }
        return true;
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        switch (args.length){
            case 1 -> completions.addAll(Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .toList());
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
