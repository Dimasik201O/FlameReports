package org.dimasik.flameReports.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.utils.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FindplayerCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if(args.length < 1){
            sender.sendMessage(Parser.color("&#00D5FC ▶ &fИспользование: &#00D5FC/findplayer [игрок]"));
            return true;
        }

        String player = args[0];
        FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(player).thenAccept(p -> {
            if(p.getServer() == null)
                sender.sendMessage(Parser.color("&#00D5FC ▶ &fИгрок оффлайн"));
            else
                sender.sendMessage(Parser.color("&#00D5FC ▶ &fИгрок &#00D5FC" + p.getNickname() + " &fнаходится на &#00D5FC" + p.getServer()));
        });
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
