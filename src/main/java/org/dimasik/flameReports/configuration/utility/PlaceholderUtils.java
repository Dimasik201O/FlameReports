package org.dimasik.flameReports.configuration.utility;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dimasik.flameReports.configuration.Pair;
import org.dimasik.flameReports.utils.Parser;

import java.util.List;

@UtilityClass
public class PlaceholderUtils {
    @SafeVarargs
    public static List<String> replace(List<String> origin, boolean applyParser, Pair<String, String>... replaces){
        return replace(null, origin, applyParser, replaces);
    }

    @SafeVarargs
    public static List<String> replace(Player source, List<String> origin, boolean applyParser, Pair<String, String>... replaces){
        return origin
                .stream()
                .map(s -> replace(source, s, applyParser, replaces))
                .toList();
    }

    @SafeVarargs
    public static String replace(String origin, boolean applyParser, Pair<String, String>... replaces){
        return replace(null, origin, applyParser, replaces);
    }

    @SafeVarargs
    public static String replace(Player source, String origin, boolean applyParser, Pair<String, String>... replaces){
        for(Pair<String, String> replace : replaces){
            while (origin.contains(replace.getLeft())) {
                origin = origin.replace(replace.getLeft(), replace.getRight());
            }
        }
        if(applyParser) origin = Parser.color(origin);
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(source, origin);
        }
        return origin;
    }
}
