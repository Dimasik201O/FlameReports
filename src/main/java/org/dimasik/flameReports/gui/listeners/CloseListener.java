package org.dimasik.flameReports.gui.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.gui.menus.Block;
import org.dimasik.flameReports.gui.menus.Close;
import org.dimasik.flameReports.models.ReportBlock;
import org.dimasik.flameReports.utils.Parser;
import org.dimasik.menus.extensions.Returnable;
import org.dimasik.menus.inheritance.Menu;
import org.dimasik.menus.inheritance.MenuListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CloseListener extends MenuListener {
    public CloseListener() {
        super(Close.class, FlameReports.getInstance(), true);
    }

    @Override
    protected void onClick(InventoryClickEvent event, Menu menu, Player player, int slot) {
        Close close = (Close) menu;
        if(slot == 11){
            FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(close.getReportBlock().getId()).thenAccept(b -> {
                if (b == null) {
                    player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    close.close();
                    return;
                }
                List<Integer> ids = Arrays.stream(close.getReportBlock().getReportIds().split(",")).map(Integer::valueOf).toList();
                for(int id : ids){
                    FlameReports.getInstance().getDatabaseManager().getReports().getReport(id).thenAccept(
                            report -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getReporterName()).thenAccept(sender -> {
                                        FlameReports.getInstance().getRedisManager().publishMessage("finish " + report.getReporterName() + " " + report.getSuspectName());
                                        FlameReports.getInstance().getDatabaseManager().getPlayers().incrementCorrectReports(sender.getId());
                                    }));

                }
                FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayerById(b.getPlayerId()).thenAccept(
                        target -> FlameReports.getInstance().getDatabaseManager().getReports().getReport(Integer.parseInt(b.getReportIds().split(",")[b.getReportIds().split(",").length - 1])).thenAccept(
                                report -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getReporterName()).thenAccept(sender -> {
                                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                    player.sendMessage(Parser.color(""));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Дело игрока &#00D5FC" + target.getNickname() + " &fуспешно закрыто."));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Заявитель: &#00D5FC" + (sender.getNickname().equalsIgnoreCase("Console") ? "System" : sender.getNickname()) + " &7(&#22FF22ПЖ: " + sender.getCorrectReports() + " &7| &#FF2222ЛЖ: " + sender.getIncorrectReports() + "&7)"));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Подозреваемый: &#00D5FC" + target.getNickname() + " &7(&#22FF22ПЖ: " + target.getCorrectReports() + " &7| &#FF2222ЛЖ: " + target.getIncorrectReports() + "&7)"));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Жалоба создана: &#00D5FC" + Parser.formatTime(report.getCreatedAt())));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Комментарий:"));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f   &#00D5FC" + report.getReason()));
                                    player.sendMessage(Parser.color("&#00D5FC ▍&f Статус: &#00D5FCЧит"));
                                    player.sendMessage(Parser.color(""));
                                }))).thenAccept((av) -> FlameReports.getInstance().getDatabaseManager().getReportBlocks().deleteReportBlock(close.getReportBlock().getId()).thenAccept((v) -> close.close()));
                ;
            });
        }
        else if(slot == 13){
            FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(close.getReportBlock().getId()).thenAccept(b -> {
                if (b == null) {
                    player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    close.close();
                    return;
                }
                FlameReports.getInstance().getDatabaseManager().getReportBlocks().setModerator(close.getReportBlock().getId(), null).thenAccept((v) -> close.close());
            });
        }
        else if(slot == 15){
            FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(close.getReportBlock().getId()).thenAccept(b -> {
                if (b == null) {
                    player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    close.close();
                    return;
                }
                List<Integer> ids = Arrays.stream(close.getReportBlock().getReportIds().split(",")).map(Integer::valueOf).toList();
                for(int id : ids){
                    FlameReports.getInstance().getDatabaseManager().getReports().getReport(id).thenAccept(
                            report -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getReporterName()).thenAccept(
                                    sender -> FlameReports.getInstance().getDatabaseManager().getPlayers().incrementIncorrectReports(sender.getId())));

                }
                FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayerById(b.getPlayerId()).thenAccept(
                        target -> FlameReports.getInstance().getDatabaseManager().getReports().getReport(Integer.parseInt(b.getReportIds().split(",")[b.getReportIds().split(",").length - 1])).thenAccept(
                                report -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getReporterName()).thenAccept(sender -> {
                                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                    player.sendMessage(Parser.color(""));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Дело игрока &#00D5FC" + target.getNickname() + " &fуспешно закрыто."));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Заявитель: &#00D5FC" + (sender.getNickname().equalsIgnoreCase("Console") ? "System" : sender.getNickname()) + " &7(&#22FF22ПЖ: " + sender.getCorrectReports() + " &7| &#FF2222ЛЖ: " + sender.getIncorrectReports() + "&7)"));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Подозреваемый: &#00D5FC" + target.getNickname() + " &7(&#22FF22ПЖ: " + target.getCorrectReports() + " &7| &#FF2222ЛЖ: " + target.getIncorrectReports() + "&7)"));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Жалоба создана: &#00D5FC" + Parser.formatTime(report.getCreatedAt())));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f Комментарий:"));
                                    player.sendMessage(Parser.color("&#00D5FC &n▍&f   &#00D5FC" + report.getReason()));
                                    player.sendMessage(Parser.color("&#00D5FC ▍&f Статус: &#00D5FCНевиновен"));
                                    player.sendMessage(Parser.color(""));
                                })));
                FlameReports.getInstance().getDatabaseManager().getReportBlocks().deleteReportBlock(close.getReportBlock().getId()).thenAccept((v) -> close.close());
            });
        }
        else if(slot == 26){
            close.close();
        }
    }

    @Override
    protected void onClose(InventoryCloseEvent event, Menu menu, Player player) {
        if(menu instanceof Returnable returnable){
            if(!returnable.isForceClose()){
                returnable.getBack().compile().open();
            }
        }
    }
}
