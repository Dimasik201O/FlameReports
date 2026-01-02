package org.dimasik.flameReports.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.database.RedisManager;
import org.dimasik.flameReports.utils.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportmuteCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if(args.length < 2){
            sender.sendMessage(Parser.color("&#FC6F00 ▶ &fИспользование: &#FC6F00/reportmute [игрок] (длительность) [причина]"));
            return true;
        }


        String target = args[0];
        if(target.length() > 16){
            sender.sendMessage(Parser.color("&#FF2222 ▶ &fИгрок не заходил на сервер за последнее время!"));
            return true;
        }
        FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(target).thenAccept(p -> {
            if (p == null) {
                sender.sendMessage(Parser.color("&#FF2222 ▶ &fИгрок не заходил на сервер за последнее время!"));
                return;
            }

            long duration = FlameReports.getInstance().getDatabaseManager().getMutes().parseTime(args[1]);
            if(duration == -1){
                String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                FlameReports.getInstance().getDatabaseManager().getMutes().addMute(p.getNickname(), reason, null).thenAccept((v) -> {
                    FlameReports.getInstance().getRedisManager().publishMessage("mute " + sender.getName() + " " + p.getNickname() + " -1 " + reason);
                });
            }
            else {
                String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                FlameReports.getInstance().getDatabaseManager().getMutes().addMute(p.getNickname(), reason, duration).thenAccept((v) -> {
                    FlameReports.getInstance().getRedisManager().publishMessage("mute " + sender.getName() + " " + p.getNickname() + " " + duration + " " + reason);
                });
            }
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
            case 2 -> {
                completions.add("1h");
                completions.add("6h");
                completions.add("1d");
                completions.add("14d");
                completions.add("30d");
            }
            case 3 -> {
                completions.add("[причина]");
            }
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
