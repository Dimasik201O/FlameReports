package org.dimasik.flameReports.handlers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.dimasik.flameReports.FlameReports;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerHandler implements PluginMessageListener {
    private boolean registered = false;

    public void registerBungeeChannel() {
        if (registered) {
            return;
        }

        try {
            FlameReports.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(FlameReports.getInstance(), "BungeeCord");
            FlameReports.getInstance().getServer().getMessenger().registerIncomingPluginChannel(FlameReports.getInstance(), "BungeeCord", this);
            registered = true;
        } catch (Exception e) {
            FlameReports.getInstance().getLogger().severe("Failed to register BungeeCord channel: " + e.getMessage());
        }
    }

    public void sendPlayerToServer(Player player, String server) {
        if (!registered) {
            FlameReports.getInstance().getLogger().warning("BungeeCord channel not registered. Call registerBungeeChannel() first.");
            return;
        }

        if (player == null || !player.isOnline()) {
            FlameReports.getInstance().getLogger().warning("Player is null or not online");
            return;
        }

        if (server == null || server.trim().isEmpty()) {
            FlameReports.getInstance().getLogger().warning("Server name cannot be null or empty");
            return;
        }

        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(FlameReports.getInstance(), "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            FlameReports.getInstance().getLogger().severe("Error sending player to server: " + e.getMessage());
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
    }
}