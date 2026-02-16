package org.dimasik.flameReports.gui.listeners;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.gui.menus.Block;
import org.dimasik.flameReports.gui.menus.Close;
import org.dimasik.flameReports.gui.menus.Main;
import org.dimasik.flameReports.models.ReportBlock;
import org.dimasik.flameReports.utils.Parser;
import org.dimasik.menus.inheritance.Menu;
import org.dimasik.menus.inheritance.MenuListener;

import java.util.List;

public class MainListener extends MenuListener {
    public MainListener() {
        super(Main.class, FlameReports.getInstance(), true);
    }

    @Override
    protected void onClick(InventoryClickEvent event, Menu menu, Player player, int slot) {
        Main main = (Main) menu;
        if(slot < 45){
            ReportBlock reportBlock = main.getData().get(slot);
            if(reportBlock != null) {
                if (event.isLeftClick()) {
                    FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlockByModerator(player.getName()).thenAccept(block -> {
                        if (block != null) {
                            player.sendMessage(Parser.color("&#FF2222▶ &fУ вас &#FF2222уже есть &fактивная жалоба &#FF2222на рассмотрении&f."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                        } else {
                            FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(reportBlock.getId()).thenAccept(b -> {
                                if (b == null) {
                                    player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                                    main.compile().open();
                                    return;
                                }
                                else if (b.getModerator() != null) {
                                    player.sendMessage(Parser.color("&#FF2222▶ &fЭта жалоба &#FF2222уже на рассмотрении&f."));
                                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                                    main.compile().open();
                                    return;
                                }
                                FlameReports.getInstance().getDatabaseManager().getReportBlocks().setModerator(reportBlock.getId(), player.getName()).thenAccept(
                                    v -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayerById(reportBlock.getPlayerId()).thenAccept(target -> {
                                    player.playSound(player, Sound.ENTITY_ALLAY_ITEM_GIVEN, 1f, 1f);
                                    main.compile().open();
                                    FlameReports.getInstance().getRedisManager().publishMessage("update " + reportBlock.getId());
                                    player.sendMessage(Parser.color("&#00D5FC▶ &fДело игрока &#00D5FC" + target.getNickname() + "&f взято на рассмотрение."));
                                    Parser.sendCopyableMessage(player, List.of("             &#00D5FC[Скопировать никнейм]"), target.getNickname(), "Нажми, чтобы скопировать");
                                }));
                            });
                        }
                    });
                } else if (event.isRightClick()) {
                    FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(reportBlock.getId()).thenAccept(b -> {
                        if (b == null) {
                            player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                            main.compile().open();
                            return;
                        }
                        player.playSound(player, Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
                        Block block = new Block(b);
                        block.setBack(menu);
                        block.setPlayer(player).compile().open();
                    });
                }
            }
        }
        else if(slot == 45){
            int newPage = main.getPage() - 1;

            int pages = FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportPageCount(player.getName(), 45, main.isOnlineOnly()).join();

            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);

            if (newPage != main.getPage()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                main.setPage(newPage);
                main.compile().open();
            }
        }
        else if(slot == 46){
            if(event.isLeftClick()){
                FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlockByModerator(player.getName()).thenAccept(reportBlock -> {
                    if(reportBlock == null)
                        return;
                    player.playSound(player, Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
                    Close close = new Close(reportBlock);
                    close.setBack(menu);
                    close.setPlayer(player).compile().open();
                });
            }
            else if (event.isRightClick()) {
                FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlockByModerator(player.getName()).thenAccept(reportBlock -> {
                    if(reportBlock == null)
                        return;
                    player.playSound(player, Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
                    Block block = new Block(reportBlock);
                    block.setBack(menu);
                    block.setPlayer(player).compile().open();
                });
            }
            else if(event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP || event.getClick() == ClickType.MIDDLE){
                FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlockByModerator(player.getName()).thenAccept(reportBlock -> {
                    if(reportBlock == null)
                        return;
                    org.dimasik.flameReports.models.Player target = FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayerById(reportBlock.getPlayerId()).join();
                    Player teleportTo = Bukkit.getPlayerExact(target.getNickname());
                    if(teleportTo == null)
                        if(target.getServer() == null)
                            player.sendMessage(Parser.color("&#00D5FC ▶ &fИгрок оффлайн"));
                        else
                            FlameReports.getInstance().getDatabaseManager().getActionsTable().setAction(player.getName(), "spectate " + target.getNickname()).thenAccept((v) -> {
                                FlameReports.getInstance().getServerHandler().sendPlayerToServer(player, target.getServer());
                                player.sendMessage(Parser.color("&#00D5FC ▶ &fТелепортация..."));
                            });
                    else {
                        Bukkit.getScheduler().runTask(FlameReports.getInstance(), () -> {
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
                        });
                    }
                });
            }
        }
        else if(slot == 47){
            int newPage = main.getPage() + 1;

            int pages = FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportPageCount(player.getName(), 45, main.isOnlineOnly()).join();

            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);

            if (newPage != main.getPage()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                main.setPage(newPage);
                main.compile().open();
            }
        }
        else if(slot == 53){
            main.setOnlineOnly(!main.isOnlineOnly());
            main.compile().open();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    @Override
    protected void onClose(InventoryCloseEvent event, Menu menu, Player player) {

    }
}
