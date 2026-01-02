package org.dimasik.flameReports.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dimasik.flameReports.FlameReports;
import org.dimasik.flameReports.utils.Parser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class RedisManager {
    private JedisPool jedisPool;
    private JedisPubSub pubSub;
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String channel;

    public RedisManager(String host, int port, String user, String password, String channel) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.channel = channel;
        connect();
        subscribeToChannel();
    }

    public void connect() {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            if(user.isEmpty()) {
                jedisPool = new JedisPool(poolConfig,
                        host,
                        port,
                        2000,
                        password);
            }
            else {
                jedisPool = new JedisPool(poolConfig,
                        host,
                        port,
                        2000,
                        user,
                        password);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribeToChannel() {
        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                try{
                    String action = message.split(" ")[0];
                    switch (action.toLowerCase()){
                        case "alert" -> {
                            String[] split = message.split(" ", 3);
                            String player = split[1];
                            String server = split[2];
                            for(Player target : Bukkit.getOnlinePlayers()){
                                if(target.hasPermission("flamereports.alert")){
                                    target.sendMessage(Parser.color(""));
                                    target.sendMessage(Parser.color("&x&F&7&6&E&0&6 &n◢&f Новая жалоба на &x&8&6&E&F&8&8" + player));
                                    target.sendMessage(Parser.color("&x&F&7&6&E&0&6 ◤&f Режим: &x&D&B&A&9&4&6" + server + " &8| &x&D&B&A&9&4&6/report list"));
                                    target.sendMessage(Parser.color(""));
                                }
                            }
                        }
                        case "finish" -> {
                            String[] split = message.split(" ", 3);
                            String receiver = split[1];
                            String player = split[2];
                            Player target = Bukkit.getPlayer(receiver);
                            if(target != null) {
                                target.sendMessage(Parser.color(""));
                                target.sendMessage(Parser.color("&#FC6F00 &n▍&f Ваша жалоба на игрока &#FC6F00" + player));
                                target.sendMessage(Parser.color("&#FC6F00 &n▍&f была &#FC6F00закрыта &fсо статусом &#FC6F00Чит"));
                                target.sendMessage(Parser.color("&#FC6F00 &n▍&f Модераторы провели &#FC6F00проверку&f, в следствии"));
                                target.sendMessage(Parser.color("&#FC6F00 ▍&f чего обнаружили &#FC6F00запрещенное ПО&f."));
                                target.sendMessage(Parser.color(""));
                                target.sendMessage(Parser.color("  &#E7E7E7&oБлагодарим за подачу жалобы и помощь в"));
                                target.sendMessage(Parser.color("   &#E7E7E7&oборьбе с недобросовестными игроками"));
                                target.sendMessage(Parser.color(""));
                            }
                        }
                        case "mute" -> {
                            String[] split = message.split(" ", 5);
                            String admin = split[1];
                            String muted = split[2];
                            String duration = FlameReports.getInstance().getDatabaseManager().getMutes().parseTime(Long.valueOf(split[3]));
                            String reason = split[4];
                            for(Player target : Bukkit.getOnlinePlayers()){
                                if(target.hasPermission("flamereports.reportmute.notify")){
                                    target.sendMessage(Parser.color("&#FC6F00[РЕПОРТ-МУТ] &fАдминистратор &#FC6F00" + admin + " &fвыдал игроку &#FC6F00" + muted + " &fпо причине '&#FC6F00" + reason + "&f' на &#FC6F00" + duration));
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        };

        new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(pubSub, channel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void publishMessage(String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(this.channel, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (pubSub != null && pubSub.isSubscribed()) {
            pubSub.unsubscribe();
        }
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
