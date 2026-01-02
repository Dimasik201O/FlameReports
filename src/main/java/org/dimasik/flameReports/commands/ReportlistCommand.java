package org.dimasik.flameReports.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.flameReports.gui.menus.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReportlistCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if(sender instanceof Player player)
            CompletableFuture.runAsync(() -> new Main().setPlayer(player).compile().open());
        return true;
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {List<String> completions = new ArrayList<>();
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
