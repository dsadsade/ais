package com.yourdomain.guardianac.checks;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.event.Listener;

public abstract class Check implements Listener {

    private final GuardianAC plugin;
    private final String name;
    private final CheckType type;
    private final boolean enabled;

    public Check(GuardianAC plugin, String name, CheckType type) {
        this.plugin = plugin;
        this.name = name;
        this.type = type;
        this.enabled = plugin.getConfig().getBoolean("checks." + type.getName() + "." + name + ".enabled", true);
    }

    public GuardianAC getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected void flag(PlayerData playerData) {
        playerData.incrementViolationLevel(name);

        int maxViolations = getPlugin().getPunishmentManager().getMaxViolations(type.getName(), name);
        int currentViolations = playerData.getViolationLevel(name);

        getPlugin().getAlertManager().sendAlert(
                getPlugin().getServer().getPlayer(playerData.getPlayerUUID()),
                playerData,
                getName()
        );

        if (currentViolations >= maxViolations) {
            getPlugin().getPunishmentManager().executePunishment(
                    getPlugin().getServer().getPlayer(playerData.getPlayerUUID()),
                    getName()
            );
            playerData.resetViolationLevel(name); // Reset violations after punishment
        }
    }
}
