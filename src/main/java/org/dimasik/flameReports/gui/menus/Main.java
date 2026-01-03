package org.dimasik.flameReports.gui.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.enums.EnumSortingType;
import org.dimasik.flameReports.models.ReportBlock;
import org.dimasik.flameReports.utils.HeadUtil;
import org.dimasik.flameReports.utils.Parser;
import org.dimasik.menus.inheritance.Menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class Main extends Menu {
    private final HashMap<Integer, ReportBlock> data = new HashMap<>();
    @Setter
    private int page = 1;
    @Setter
    private EnumSortingType sortingType = EnumSortingType.OLDEST_FIRST;
    @Setter
    private boolean onlineOnly = true;

    public Main() {
        super(FlameReports.getInstance());
    }

    @Override
    public Menu compile() {
        data.clear();
        List<ReportBlock> list = FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlocks(viewer.getName(), page, 45, sortingType, onlineOnly).join();
        int pages = FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportPageCount(viewer.getName(), 45, onlineOnly).join();
        inventory = Bukkit.createInventory(this, 54, "Жалобы (" + pages + "/" + pages + ")");

        for(int slot = 0; slot < 45 && slot < list.size(); slot++) {
            final int finalSlot = slot;
            ReportBlock reportBlock = list.get(slot);
            FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayerById(reportBlock.getPlayerId()).thenAccept(
                    target -> FlameReports.getInstance().getDatabaseManager().getReports().getReport(Integer.parseInt(reportBlock.getReportIds().split(",")[reportBlock.getReportIds().split(",").length - 1])).thenAccept(
                            report -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getReporterName()).thenAccept(sender -> {
                                        ItemStack itemStack = HeadUtil.setPlayer(new ItemStack(sender.getNickname().equalsIgnoreCase("Console") ? Material.DRAGON_HEAD : Material.PLAYER_HEAD, reportBlock.getReportIds().split(",").length), target.getNickname());
                                        ItemMeta itemMeta = itemStack.getItemMeta();
                                        itemMeta.setDisplayName(Parser.color(" &#00D5FC❑ Дело игрока " + target.getNickname() + (target.getServer() != null ? " &8(" + target.getServer() + ")" : "") + " &#00D5FC❑"));
                                        List<String> lore = new ArrayList<>();
                                        lore.add(Parser.color(""));
                                        lore.add(Parser.color("&#00D5FC &n▍&f Заявитель: &#00D5FC" + (sender.getNickname().equalsIgnoreCase("Console") ? "System" : sender.getNickname()) + " &7(&#22FF22ПЖ: " + sender.getCorrectReports() + " &7| &#FF2222ЛЖ: " + sender.getIncorrectReports() + "&7)"));
                                        lore.add(Parser.color("&#00D5FC &n▍&f Подозреваемый: &#00D5FC" + target.getNickname() + " &7(&#22FF22ПЖ: " + target.getCorrectReports() + " &7| &#FF2222ЛЖ: " + target.getIncorrectReports() + "&7)"));
                                        lore.add(Parser.color("&#00D5FC &n▍&f Жалоба создана: &#00D5FC" + Parser.formatTime(report.getCreatedAt())));
                                        lore.add(Parser.color("&#00D5FC &n▍&f Комментарий:"));
                                        lore.add(Parser.color("&#00D5FC ▍&f   &#00D5FC" + report.getReason()));
                                        lore.add(Parser.color(""));
                                        lore.add(Parser.color(" &#00D5FC▶ [ЛКМ] &fВзять все жалобы на рассмотрение"));
                                        lore.add(Parser.color(" &#00D5FC&n&l◢&#00D5FC [ПКМ] &fПосмотреть все жалобы"));
                                        lore.add(Parser.color(" &#00D5FC&l◤&#00D5FC           &fПодано &#00D5FC" + reportBlock.getReportIds().split(",").length + " жалоб"));
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
        FlameReports.getInstance().getDatabaseManager().getReportBlocks().getReportBlock(viewer.getName()).thenAccept(reportBlock -> {
            if(reportBlock == null){
                ItemStack itemStack = new ItemStack(Material.RED_CANDLE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color(" "));
                itemMeta.setLore(List.of(
                        Parser.color("&#FF2222 ▶ &fУ вас нет жалобы на рассмотрении"),
                        Parser.color("")
                ));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(46, itemStack);
            }
            else{
                FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayerById(reportBlock.getPlayerId()).thenAccept(
                        p -> FlameReports.getInstance().getDatabaseManager().getReports().getReport(Integer.parseInt(reportBlock.getReportIds().split(",")[reportBlock.getReportIds().split(",").length - 1])).thenAccept(
                                report -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getReporterName()).thenAccept(
                                        sender -> FlameReports.getInstance().getDatabaseManager().getPlayers().getPlayer(report.getSuspectName()).thenAccept(target -> {
                                            ItemStack itemStack = HeadUtil.setPlayer(new ItemStack(sender.getNickname().equalsIgnoreCase("Console") ? Material.DRAGON_HEAD : Material.PLAYER_HEAD, reportBlock.getReportIds().split(",").length), p.getNickname());
                                            ItemMeta itemMeta = itemStack.getItemMeta();
                                            itemMeta.setDisplayName(Parser.color(" &#00D5FC❑ Дело игрока " + p.getNickname() + (p.getServer() != null ? " &8(" + p.getServer() + ")" : "") + " &#00D5FC❑"));
                                            List<String> lore = new ArrayList<>();
                                            lore.add(Parser.color(""));
                                            lore.add(Parser.color("&#00D5FC &n▍&f Заявитель: &#00D5FC" + (sender.getNickname().equalsIgnoreCase("Console") ? "System" : sender.getNickname()) + " &7(&#22FF22ПЖ: " + sender.getCorrectReports() + " &7| &#FF2222ЛЖ: " + sender.getIncorrectReports() + "&7)"));
                                            lore.add(Parser.color("&#00D5FC &n▍&f Подозреваемый: &#00D5FC" + target.getNickname() + " &7(&#22FF22ПЖ: " + target.getCorrectReports() + " &7| &#FF2222ЛЖ: " + target.getIncorrectReports() + "&7)"));
                                            lore.add(Parser.color("&#00D5FC &n▍&f Жалоба создана: &#00D5FC" + Parser.formatTime(report.getCreatedAt())));
                                            lore.add(Parser.color("&#00D5FC &n▍&f Комментарий:"));
                                            lore.add(Parser.color("&#00D5FC ▍&f   &#00D5FC" + report.getReason()));
                                            lore.add(Parser.color(""));
                                            lore.add(Parser.color(" &#00D5FC▶ [ЛКМ] &fЗакрыть со статусом"));
                                            lore.add(Parser.color(" &#00D5FC&n&l◢&#00D5FC [ПКМ] &fПосмотреть все жалобы"));
                                            lore.add(Parser.color(" &#00D5FC&l◤&#00D5FC           &fПодано &#00D5FC" + reportBlock.getReportIds().split(",").length + " жалоб"));
                                            itemMeta.setLore(lore);
                                            itemStack.setItemMeta(itemMeta);
                                            inventory.setItem(46, itemStack);
                                        }))));
            }
        });
        {
            ItemStack itemStack = new ItemStack(Material.LIME_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color("&6Следующая страница ▶"));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(47, itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setHideTooltip(true);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(48, itemStack);
            inventory.setItem(49, itemStack);
            inventory.setItem(50, itemStack);
            inventory.setItem(51, itemStack);
            inventory.setItem(52, itemStack);
        }
        if(onlineOnly){
            ItemStack itemStack = new ItemStack(Material.MUSIC_DISC_TEARS);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color(" "));
            itemMeta.setLore(List.of(
                    Parser.color("&#00D5FC ● &fОтображение репортов где"),
                    Parser.color("&0.&x&D&5&D&B&D&C   подозреваемый &#00D5FCонлайн"),
                    Parser.color("")
            ));
            itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(53, itemStack);
        }
        else{
            ItemStack itemStack = new ItemStack(Material.MUSIC_DISC_CREATOR_MUSIC_BOX);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color(" "));
            itemMeta.setLore(List.of(
                    Parser.color("&#FF2222 ● &fОтображение репортов где"),
                    Parser.color("&0.&x&D&5&D&B&D&C   подозреваемый &#FF2222оффлайн"),
                    Parser.color("")
            ));
            itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(53, itemStack);
        }
        return this;
    }
}
