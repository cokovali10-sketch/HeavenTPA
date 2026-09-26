package com.vitalis.tpa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

/** HeavenTPA v1.0.0 - Paper/Purpur 1.21.11. Chat-button teleport requests. */
public class HeavenTPA extends JavaPlugin implements Listener {
    private final TpaConfig cfg = new TpaConfig();
    private final Requests requests = new Requests();

    @Override
    public void onEnable() {
        cfg.load(this);
        try {
            registerCommand("tpa", "Ask to teleport to a player",
                    java.util.List.of(), new TpaCommand(this, "tpa"));
            registerCommand("tpaccept", "Accept a teleport request",
                    java.util.List.of("tpyes"), new TpaCommand(this, "tpaccept"));
            registerCommand("tpdeny", "Decline a teleport request",
                    java.util.List.of("tpno"), new TpaCommand(this, "tpdeny"));
            registerCommand("tpareload", "Reload config",
                    java.util.List.of(), new TpaCommand(this, "reload"));
        } catch (NoSuchMethodError e) {
            getLogger().severe("Brigadier register unavailable, commands disabled.");
        }
        getServer().getPluginManager().registerEvents(this, this);
        // expiry sweeper every 10s
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            List<UUID[]> dead = requests.expire(System.currentTimeMillis());
            if (dead.isEmpty()) return;
            getServer().getScheduler().runTask(this, () -> {
                for (UUID[] pair : dead) {
                    Player t = Bukkit.getPlayer(pair[0]);
                    Player f = Bukkit.getPlayer(pair[1]);
                    if (t != null) {
                        t.sendMessage(Msg.color(cfg.prefix + "&7Запрос истёк."));
                    }
                    if (f != null) {
                        f.sendMessage(Msg.color(cfg.prefix + "&7Запрос игроку &f"
                                + (t != null ? t.getName() : "?") + " &7истёк."));
                    }
                }
            });
        }, 200L, 200L);
        getLogger().info("HeavenTPA enabled: requests expire in " + cfg.expireSeconds + "s.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // drop requests TO the leaver and FROM the leaver: no ghost accepts
        requests.remove(e.getPlayer().getUniqueId(), e.getPlayer().getUniqueId());
        requests.clearSender(e.getPlayer().getUniqueId());
    }

    public TpaConfig cfg() { return cfg; }
    public Requests requests() { return requests; }
}
