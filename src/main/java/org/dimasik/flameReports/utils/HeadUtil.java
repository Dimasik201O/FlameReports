package org.dimasik.flameReports.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

@UtilityClass
public class HeadUtil {
    public static ItemStack setPlayer(ItemStack head, String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return head;
        }

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        head.setItemMeta(skullMeta);
        return head;
    }
}