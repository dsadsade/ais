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
        // This saves the default config.yml from the JAR to the plugin's folder if it doesn't exist
        plugin.saveDefaultConfig();
        // This reloads the config from the file
        plugin.reloadConfig();
        // This gets the reloaded config
        config = plugin.getConfig();
        // Set defaults if they are missing
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public String getPrefix() {
        return config.getString("prefix", "&8[&cGuardianAC &c⚔️&8] ");
    }

    public boolean isCheckEnabled(String checkName) {
        return config.getBoolean("checks." + checkName + ".enabled", true);
    }
}
