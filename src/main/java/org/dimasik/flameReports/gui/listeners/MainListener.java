package org.dimasik.flameReports.gui.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
                    FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(player.getName()).thenAccept(block -> {
                        if (block != null) {
                            player.sendMessage(Parser.color("&#FF2222▶ &fУ вас &#FF2222уже есть &fактивная жалоба &#FF2222на рассмотрении&f."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                        } else {
                            FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(reportBlock.getId()).thenAccept(b -> {
                                if (b == null) {
                                    player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                                    return;
                                }
                                else if (b.getModerator() != null) {
                                    player.sendMessage(Parser.color("&#FF2222▶ &fЭта жалоба &#FF2222уже на рассмотрении&f."));
                                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                                    return;
                                }
                                FlameReports.getInstance().getDatabaseManager().getReportBlocks().setModerator(reportBlock.getId(), player.getName()).thenAccept(av -> {
                                    player.playSound(player, Sound.ENTITY_ALLAY_ITEM_GIVEN, 1f, 1f);
                                    main.compile().open();
                                });
                            });
                        }
                    });
                } else if (event.isRightClick()) {
                    FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(reportBlock.getId()).thenAccept(b -> {
                        if (b == null) {
                            player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
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
                FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(player.getName()).thenAccept(reportBlock -> {
                    if(reportBlock == null)
                        return;
                    FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(reportBlock.getId()).thenAccept(b -> {
                        if (b == null) {
                            player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                            return;
                        }
                        player.playSound(player, Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
                        Close close = new Close(b);
                        close.setBack(menu);
                        close.setPlayer(player).compile().open();
                    });
                });
            }
            else if (event.isRightClick()) {
                FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(player.getName()).thenAccept(reportBlock -> {
                    if(reportBlock == null)
                        return;
                    FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(reportBlock.getId()).thenAccept(b -> {
                        if (b == null) {
                            player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                            return;
                        }
                        player.playSound(player, Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
                        Block block = new Block(b);
                        block.setBack(menu);
                        block.setPlayer(player).compile().open();
                    });
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
