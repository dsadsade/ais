package com.yourdomain.guardianac;

import com.yourdomain.guardianac.checks.CheckManager;
import com.yourdomain.guardianac.commands.GuardianCommand;
import com.yourdomain.guardianac.listeners.PlayerListener;
import com.yourdomain.guardianac.managers.AlertManager;
import com.yourdomain.guardianac.managers.ConfigManager;
import com.yourdomain.guardianac.managers.PunishmentManager;
import com.yourdomain.guardianac.player.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuardianAC extends JavaPlugin {

    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        playerDataManager = new PlayerDataManager();
        getServer().getPluginManager().registerEvents(new PlayerListener(playerDataManager), this);

        checkManager = new CheckManager(this);
        punishmentManager = new PunishmentManager(this);
        alertManager = new AlertManager(this);
        checkManager.registerChecks();

        getCommand("guardian").setExecutor(new GuardianCommand(this));

        getLogger().info("GuardianAC has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GuardianAC has been disabled!");
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
}
