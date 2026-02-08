package com.yourdomain.guardianac.managers;

import com.yourdomain.guardianac.GuardianAC;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final GuardianAC plugin;
    private FileConfiguration config;

    public ConfigManager(GuardianAC plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public String getPrefix() {
        return config.getString("prefix", "&8[&cGuardianAC &c⚔️&8] ");
    }

    public boolean isCheckEnabled(String checkName) {
        return config.getBoolean("checks." + checkName + ".enabled", true);
    }

    public long getViolationDecayIntervalMs() {
        return config.getLong("violation-decay.interval-seconds", 30) * 1000;
    }

    public long getViolationDecayAfterMs() {
        return config.getLong("violation-decay.decay-after-seconds", 60) * 1000;
    }
}
