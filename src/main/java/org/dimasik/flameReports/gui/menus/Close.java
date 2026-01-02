package org.dimasik.flameReports.gui.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.models.ReportBlock;
import org.dimasik.flameReports.utils.Parser;
import org.dimasik.menus.extensions.Returnable;
import org.dimasik.menus.implementation.IMenu;
import org.dimasik.menus.inheritance.Menu;

import java.util.List;

@Getter
public class Close extends Menu implements Returnable {
    private final ReportBlock reportBlock;
    @Setter
    private boolean forceClose = false;
    @Setter
    private IMenu back;

    public Close(ReportBlock reportBlock) {
        super(FlameReports.getInstance());
        this.reportBlock = reportBlock;
    }

    @Override
    public Menu compile() {
        inventory = Bukkit.createInventory(this, 27, "Изменение статуса жалобы");
        {
            ItemStack itemStack = new ItemStack(Material.LIME_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color(" "));
            itemMeta.setLore(List.of(
                    Parser.color("&#22FF22 ● &fЗакрыть жалобу со"),
                    Parser.color("&0.&x&D&5&D&B&D&C   статусом: &#22FF22Положительная"),
                    Parser.color("")
            ));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(11, itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.ORANGE_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color(" "));
            itemMeta.setLore(List.of(
                    Parser.color("&6 ▶ &fСнять жалобу с рассмотрения"),
                    Parser.color("")
            ));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(13, itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.RED_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color(" "));
            itemMeta.setLore(List.of(
                    Parser.color("&#FF2222 ● &fЗакрыть жалобу со"),
                    Parser.color("&0.&x&D&5&D&B&D&C   статусом: &#FF2222Ложная"),
                    Parser.color("")
            ));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(15, itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.ARROW);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color("&#FF2222◀ &fНазад"));
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(26, itemStack);
        }
        return this;
    }
}
