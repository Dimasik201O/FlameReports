package org.dimasik.flameReports.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.configuration.ConfigManager;
import org.dimasik.flameReports.utils.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheatreportCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if(args.length < 2){
            sender.sendMessage(Parser.color(""));
            sender.sendMessage(Parser.color("&#FC6F00&l          ▼ СИСТЕМА ЖАЛОБ НА ЧИТЕРОВ ▼"));
            sender.sendMessage(Parser.color(""));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f Через команду &#00D5FC/cheatreport &fты можешь оставить жалобу"));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f только &#00D5FCна игрока с читами&f. Пожалуйста, оставляй"));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f жалобы только на читеров, &#00D5FCостальные типы жалоб"));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f рассматриваются по-прежнему &#00D5FCв группе&f."));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f "));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f Одними из первых рассматриваются жалобы с весомыми"));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f доказательствами. Весомым доказательством считается"));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f - ссылка на &#00D5FCвидео/скриншоты&f, где четко запечатлено"));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f &#00D5FCиспользование читов игроком&f."));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f "));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f Помни, что использование данной системы &#00D5FCради своей"));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f &#00D5FCвыгоды &#00D5FCнеприемлемо и &#00D5FCнаказывается мутом на подачи"));
            sender.sendMessage(Parser.color("&#00D5FC &n▍&f любых жалоб. &#00D5FCОставляй жалобы &#00D5FCтолько на игроков, про"));
            sender.sendMessage(Parser.color("&#00D5FC ▍&f которых ты &#00D5FCуверен, что они используют ЧИТЫ&#00D5FC."));
            sender.sendMessage(Parser.color(""));
            sender.sendMessage(Parser.color("&#FC6F00&l    ▲ ПРОЧИТАЙ ВЫШЕ, ПЕРЕД ИСПОЛЬЗОВАНИЕМ ▲"));
            sender.sendMessage(Parser.color(""));
            sender.sendMessage(Parser.color("&#00D5FC ▶ &fИспользование команды:"));
            sender.sendMessage(Parser.color("    &#E7E7E7/cheatreport (ник) (комментарий с доказательством)."));
            sender.sendMessage(Parser.color(""));
            return true;
        }

        FlameReports.getInstance().getDatabaseManager().getMutes().checkMute(sender.getName()).thenAccept((mute) -> {
            if(mute == null){
                String target = args[0];
                if(target.length() > 16){
                    sender.sendMessage(Parser.color("&#FF2222 ▶ &fИгрок не заходил на сервер за последнее время!"));
                    return;
                }
                if(target.equalsIgnoreCase(sender.getName())){
                    sender.sendMessage(Parser.color("&#FF2222▶ &fВы не можете подавать жалобы на самого себя."));
                    return;
                }
                FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(target).thenAccept(p -> {
                    if(p == null){
                        sender.sendMessage(Parser.color("&#FF2222 ▶ &fИгрок не заходил на сервер за последнее время!"));
                        return;
                    }
                    FlameReports.getInstance().getDatabaseManager().getReports().hasReport(sender.getName(), target).thenAccept(b -> {
                        if(sender instanceof Player && b) {
                            sender.sendMessage(Parser.color("&#FF2222 ▶ &fВы уже отправили жалобу на этого игрока."));
                            return;
                        }
                        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                        FlameReports.getInstance().getDatabaseManager().executeAddReportTransaction(target, sender.getName(), reason).thenAccept(
                                integer -> sender.sendMessage(Parser.color("&#00D5FC ▶ &fВаша жалоба &#00D5FC#" + integer + "&f была &#00D5FCпринята&f. Наши сотрудники &#00D5FCрассмотрят ваш случай и проведут соответствующую проверку&f."))).thenAccept(
                                        (v) -> {
                                            if(sender instanceof Player)
                                                FlameReports.getInstance().getRedisManager().publishMessage("alert " + p.getNickname() + " " + ConfigManager.getString("config.yml", "server.mode", "unknown"));
                                        });
                    });
                });
                return;
            }
            String time = FlameReports.getInstance().getDatabaseManager().getMutes().parseTime(mute.getSecondsLeft());
            sender.sendMessage(Parser.color(""));
            sender.sendMessage(Parser.color("&#FC6F00 &n▍&f В данный момент у вас отсутствует"));
            sender.sendMessage(Parser.color("&#FC6F00 &n▍&f возможность отправлять жалобы, так"));
            sender.sendMessage(Parser.color("&#FC6F00 &n▍&f как вы получили репорт-мут по причине"));
            sender.sendMessage(Parser.color("&#FC6F00 &n▍&#FC6F00     " + mute.getReason()));
            sender.sendMessage(Parser.color("&#FC6F00 &n▍&f"));
            sender.sendMessage(Parser.color("&#FC6F00 ▍&f Мут выдан на: &#FC6F00" + time));
            sender.sendMessage(Parser.color(""));
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        switch (args.length){
            case 1 -> completions.addAll(Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .toList());
            case 2 -> completions.add("[причина]");
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
