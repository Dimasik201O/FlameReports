package org.dimasik.flameReports.gui.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.models.Player;
import org.dimasik.flameReports.models.ReportBlock;
import org.dimasik.flameReports.utils.HeadUtil;
import org.dimasik.flameReports.utils.Parser;
import org.dimasik.menus.extensions.Returnable;
import org.dimasik.menus.implementation.IMenu;
import org.dimasik.menus.inheritance.Menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Getter
public class Block extends Menu implements Returnable {
    private final ReportBlock reportBlock;
    private final HashMap<Integer, ReportBlock> data = new HashMap<>();
    @Setter
    private int page = 1;
    @Setter
    private boolean forceClose = false;
    @Setter
    private IMenu back;

    public Block(ReportBlock reportBlock) {
        super(FlameReports.getInstance());
        this.reportBlock = reportBlock;
    }

    @Override
    public Menu compile() {
        Player player = FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayerById(reportBlock.getPlayerId()).join();
        inventory = Bukkit.createInventory(this, 54, "Дело игрока " + player.getNickname());
        List<Integer> ids = Arrays.stream(reportBlock.getReportIds().split(",")).map(Integer::valueOf).toList();
        for(int slot = 0; slot < 45 && slot < ids.size(); slot++){
            final int finalSlot = slot;
            FlameReports.getInstance().getDatabaseManager().getReports().getReport(ids.get(slot)).thenAccept(
                    report -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getReporterName()).thenAccept(
                            sender -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getSuspectName()).thenAccept(target -> {
                                ItemStack itemStack = HeadUtil.setPlayer(new ItemStack(Material.PLAYER_HEAD), sender.getNickname());
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                itemMeta.setDisplayName(Parser.color(" "));
                                List<String> lore = new ArrayList<>();
                                lore.add(Parser.color("&#00D5FC &n▍&f Заявитель: &#00D5FC" + sender.getNickname() + " &7(&#22FF22ПЖ: " + sender.getCorrectReports() + " &7| &#FF2222ЛЖ: " + sender.getIncorrectReports() + "&7)"));
                                lore.add(Parser.color("&#00D5FC &n▍&f Жалоба создана: &#00D5FC" + Parser.formatTime(report.getCreatedAt())));
                                lore.add(Parser.color("&#00D5FC &n▍&f Комментарий:"));
                                lore.add(Parser.color("&#00D5FC ▍&f   &#00D5FC" + report.getReason()));
                                lore.add(Parser.color(""));
                                lore.add(Parser.color("   &8&o#" + report.getId()));
                                itemMeta.setLore(lore);
                                itemStack.setItemMeta(itemMeta);
                                inventory.setItem(finalSlot, itemStack);
                                data.put(finalSlot, reportBlock);
                            })));
        }
        {
            ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F◀ Предыдущая страница"));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(45, itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.LIME_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color("&6Следующая страница ▶"));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(46, itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setHideTooltip(true);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(47, itemStack);
            inventory.setItem(48, itemStack);
            inventory.setItem(49, itemStack);
            inventory.setItem(50, itemStack);
            inventory.setItem(51, itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.POPPED_CHORUS_FRUIT);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color("&#22FF22▶ &fВзять все жалобы на рассмотрение"));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(52, itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.ARROW);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color("&#FF2222◀ &fНазад"));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(53, itemStack);
        }
        return this;
    }
}
