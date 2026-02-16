package org.dimasik.flameReports;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.flameReports.commands.*;
import org.dimasik.flameReports.configuration.ConfigManager;
import org.dimasik.flameReports.database.DatabaseManager;
import org.dimasik.flameReports.database.RedisManager;
import org.dimasik.flameReports.gui.listeners.BlockListener;
import org.dimasik.flameReports.gui.listeners.CloseListener;
import org.dimasik.flameReports.gui.listeners.MainListener;
import org.dimasik.flameReports.handlers.ServerHandler;
import org.dimasik.flameReports.listeners.PlayerListener;

import java.io.File;

@Getter
public final class FlameReports extends JavaPlugin {
    @Getter
    private static FlameReports instance;
    private DatabaseManager databaseManager;
    private RedisManager redisManager;
    private ServerHandler serverHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        setupConfig();
        setupDatabase();
        setupRedis();
        serverHandler = new ServerHandler();
        serverHandler.registerBungeeChannel();

        registerCommand("cheatreport", new CheatreportCommand());
        registerCommand("reportlist", new ReportlistCommand());
        registerCommand("reportmute", new ReportmuteCommand());
        registerCommand("findplayer", new FindplayerCommand());
        registerCommand("globalteleport", new GlobalteleportCommand());

        var pm = super.getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new MainListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new CloseListener(), this);

        for(Player player : Bukkit.getOnlinePlayers())
            FlameReports.getInstance().getDatabaseManager().getPlayers().setOrCreateServer(
                    player.getName(),
                    ConfigManager.getString("config.yml", "server.mode", "unknown")
            );
    }

    private void registerCommand(String command, TabExecutor tabExecutor){
        var cmd = super.getCommand(command);
        if(cmd == null){
            return;
        }
        cmd.setExecutor(tabExecutor);
        cmd.setTabCompleter(tabExecutor);
    }

    private void setupConfig(){
        String[] files = {
                "config.yml",
        };
        for(String filePath : files) {
            File file = new File(super.getDataFolder(), filePath);
            if (!file.exists()) {
                super.saveResource(filePath, false);
            }
        }
        ConfigManager.init(this);
    }

    private void setupDatabase(){
        databaseManager = new DatabaseManager(
                ConfigManager.getString("config.yml", "mysql.host", "localhost"),
                ConfigManager.getString("config.yml", "mysql.user", "root"),
                ConfigManager.getString("config.yml", "mysql.password", "сайнес гпт кодер"),
                ConfigManager.getString("config.yml", "mysql.database", "reports")
        );
    }

    private void setupRedis(){
        redisManager = new RedisManager(
                ConfigManager.getString("config.yml", "redis.host", "localhost"),
                ConfigManager.getInt("config.yml", "redis.port", 6379),
                ConfigManager.getString("config.yml", "redis.user", "сайнес гпт кодер"),
                ConfigManager.getString("config.yml", "redis.password", "password"),
                ConfigManager.getString("config.yml", "redis.channel", "core")
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(Player player : Bukkit.getOnlinePlayers())
            FlameReports.getInstance().getDatabaseManager().getPlayers().setOrCreateServer(
                    player.getName(), null
            );
        if(redisManager != null)
            redisManager.close();
    }
}
