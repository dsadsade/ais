package com.yourdomain.guardianac;

import com.yourdomain.guardianac.checks.CheckManager;
import com.yourdomain.guardianac.commands.GuardianCommand;
import com.yourdomain.guardianac.commands.GuardianTabCompleter;
import com.yourdomain.guardianac.gui.GuiListener;
import com.yourdomain.guardianac.gui.GuiManager;
import com.yourdomain.guardianac.listeners.PlayerListener;
import com.yourdomain.guardianac.managers.AlertManager;
import com.yourdomain.guardianac.managers.ConfigManager;
import com.yourdomain.guardianac.managers.PunishmentManager;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.player.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class GuardianAC extends JavaPlugin {

    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;
    private GuiManager guiManager;
    private BukkitTask decayTask;

    @Override
    public void onEnable() {
        // Managers
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        playerDataManager = new PlayerDataManager();
        punishmentManager = new PunishmentManager(this);
        alertManager = new AlertManager(this);
        guiManager = new GuiManager(this);

        // Checks
        checkManager = new CheckManager(this);
        checkManager.registerChecks();

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        // Commands
        GuardianCommand commandExecutor = new GuardianCommand(this);
        getCommand("guardian").setExecutor(commandExecutor);
        getCommand("guardian").setTabCompleter(new GuardianTabCompleter(this));

        // Violation decay task
        startDecayTask();

        getLogger().info("GuardianAC v" + getDescription().getVersion() + " enabled — " + checkManager.getChecks().size() + " checks loaded.");
    }

    @Override
    public void onDisable() {
        if (decayTask != null) decayTask.cancel();
        getLogger().info("GuardianAC disabled.");
    }

    public void startDecayTask() {
        if (decayTask != null) decayTask.cancel();
        long decayIntervalMs = configManager.getViolationDecayIntervalMs();
        long decayAfterMs = configManager.getViolationDecayAfterMs();
        decayTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                PlayerData data = playerDataManager.getPlayerData(player);
                if (data != null) {
                    data.decayViolations(decayAfterMs);
                }
            }
        }, 20L * 30, 20L * (decayIntervalMs / 1000));
    }

    // ── Getters ──
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public AlertManager getAlertManager() { return alertManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public CheckManager getCheckManager() { return checkManager; }
    public GuiManager getGuiManager() { return guiManager; }
}
