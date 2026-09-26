package com.vitalis.tpa;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Typed config: prefix, sounds, expiry. */
public class TpaConfig {
    public String prefix;
    public boolean sounds;
    public int expireSeconds;
    public boolean exact;

    public void load(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();
        prefix = c.getString("general.prefix", "&8[&bТП&8] ");
        sounds = c.getBoolean("general.sounds", true);
        expireSeconds = Math.max(10, c.getInt("general.expire-seconds", 60));
        exact = c.getBoolean("general.exact", true);
    }
}
