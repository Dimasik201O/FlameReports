package org.dimasik.flameReports.gui.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.gui.menus.Block;
import org.dimasik.flameReports.models.ReportBlock;
import org.dimasik.flameReports.utils.Parser;
import org.dimasik.menus.extensions.Returnable;
import org.dimasik.menus.inheritance.Menu;
import org.dimasik.menus.inheritance.MenuListener;

public class BlockListener extends MenuListener {
    public BlockListener() {
        super(Block.class, FlameReports.getInstance(), true);
    }

    @Override
    protected void onClick(InventoryClickEvent event, Menu menu, Player player, int slot) {
        Block block = (Block) menu;
        if(slot == 45){
            int newPage = block.getPage() - 1;

            ReportBlock b = FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(block.getReportBlock().getId()).join();
            if (b == null) {
                player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                block.close();
                return;
            }
            int pages = b.getReportIds().split(",").length;

            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);

            if (newPage != block.getPage()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                block.setForceClose(true);
                Block newBlock = new Block(block.getReportBlock());
                newBlock.setBack(block.getBack());
                newBlock.setPage(newPage);
                newBlock.setPlayer(player);
                newBlock.compile().open();
            }
        }
        else if(slot == 46){
            int newPage = block.getPage() + 1;

            ReportBlock b = FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(block.getReportBlock().getId()).join();
            if (b == null) {
                player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                block.close();
                return;
            }
            int pages = b.getReportIds().split(",").length;

            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);

            if (newPage != block.getPage()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                block.setForceClose(true);
                Block newBlock = new Block(block.getReportBlock());
                newBlock.setBack(block.getBack());
                newBlock.setPage(newPage);
                newBlock.setPlayer(player);
                newBlock.compile().open();
            }
        }
        else if(slot == 52){
            FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(player.getName()).thenAccept(rb -> {
                if (rb != null) {
                    player.sendMessage(Parser.color("&#FF2222▶ &fУ вас &#FF2222уже есть &fактивная жалоба &#FF2222на рассмотрении&f."));
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                } else {
                    FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(block.getReportBlock().getId()).thenAccept(b -> {
                        if (b == null) {
                            player.sendMessage(Parser.color("&#FF2222▶ &fЖалоба &#FF2222больше не существует&f."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                            block.close();
                            return;
                        }
                        else if (b.getModerator() != null) {
                            player.sendMessage(Parser.color("&#FF2222▶ &fЭта жалоба &#FF2222уже на рассмотрении&f."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                            return;
                        }
                        FlameReports.getInstance().getDatabaseManager().getReportBlocks().setModerator(block.getReportBlock().getId(), player.getName());
                        player.playSound(player, Sound.ENTITY_ALLAY_ITEM_GIVEN, 1f, 1f);
                        block.close();
                    });
                }
            });
        }
        else if(slot == 53){
            block.close();
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
