package com.yourdomain.guardianac.checks;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Base class for all anticheat checks.
 * Supports sub-typed checks (e.g. KillAura A, KillAura B) with a buffer system
 * that absorbs occasional false positives and only flags on sustained detections.
 */
public abstract class Check implements Listener {

    private final GuardianAC plugin;
    private final String name;
    private final String subType;
    private final CheckType type;
    private boolean enabled;
    private final String configKey;
    private final String violationKey;

    /** Constructor for single checks (no sub-type). */
    public Check(GuardianAC plugin, String name, CheckType type) {
        this(plugin, name, null, type);
    }

    /** Constructor for sub-typed checks (e.g. KillAura "A"). */
    public Check(GuardianAC plugin, String name, String subType, CheckType type) {
        this.plugin = plugin;
        this.name = name;
        this.subType = subType;
        this.type = type;

        if (subType != null) {
            this.configKey = type.getName() + "." + name + "." + subType;
            this.violationKey = name + "_" + subType;
        } else {
            this.configKey = type.getName() + "." + name;
            this.violationKey = name;
        }

        this.enabled = plugin.getConfig().getBoolean("checks." + configKey + ".enabled", true);
    }

    public GuardianAC getPlugin() { return plugin; }
    public String getName() { return name; }
    public String getSubType() { return subType; }
    public CheckType getCheckType() { return type; }
    public String getConfigKey() { return configKey; }
    public String getViolationKey() { return violationKey; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /** Display name for alerts (e.g. "KillAura (A)"). */
    public String getDisplayName() {
        return subType != null ? name + " (" + subType + ")" : name;
    }

    protected boolean isExempt(Player player) {
        return player.hasPermission("guardian.bypass");
    }

    /**
     * Buffer-based violation handler. Adds to a per-check buffer and only flags
     * when the buffer exceeds the threshold. This dramatically reduces false positives.
     * Buffer thresholds are automatically scaled with player ping.
     *
     * @param data            The player data
     * @param player          The player being checked
     * @param violationWeight How much to add to the buffer (higher = more certain)
     * @param bufferLimit     Buffer threshold before flagging
     * @param bufferDecay     How much to subtract from buffer per call (natural decay)
     */
    protected void handleViolation(PlayerData data, Player player, double violationWeight,
                                    double bufferLimit, double bufferDecay) {
        if (isExempt(player)) return;

        double adjustedLimit = bufferLimit * PingUtil.getBufferMultiplier(player);
        double buffer = data.getCheckBuffer(violationKey);
        buffer = Math.max(0, buffer + violationWeight - bufferDecay);
        data.setCheckBuffer(violationKey, buffer);

        if (buffer >= adjustedLimit) {
            flag(data);
            data.setCheckBuffer(violationKey, Math.max(0, buffer - adjustedLimit * 0.75));
        }
    }

    /** Direct flag for checks that are already internally buffered. */
    protected void flag(PlayerData playerData) {
        Player player = getPlugin().getServer().getPlayer(playerData.getPlayerUUID());
        if (player == null || !player.isOnline()) return;
        if (isExempt(player)) return;

        playerData.incrementViolationLevel(violationKey);

        int maxViolations = getPlugin().getPunishmentManager().getMaxViolations(configKey);
        int currentViolations = playerData.getViolationLevel(violationKey);

        getPlugin().getAlertManager().sendAlert(player, playerData, getDisplayName(), violationKey);

        if (currentViolations >= maxViolations) {
            getPlugin().getPunishmentManager().executePunishment(player, getDisplayName());
            playerData.resetViolationLevel(violationKey);
        }
    }

    /**
     * Convenience flag method that resolves PlayerData from a Player.
     * Includes an optional debug info string for logging/development.
     *
     * @param player The player being flagged
     * @param debugInfo Additional context about the detection (e.g. "speed=0.42 max=0.32")
     */
    protected void flag(Player player, String debugInfo) {
        if (player == null || !player.isOnline()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.incrementViolationLevel(violationKey);

        int maxViolations = getPlugin().getPunishmentManager().getMaxViolations(configKey);
        int currentViolations = data.getViolationLevel(violationKey);

        getPlugin().getAlertManager().sendAlert(player, data, getDisplayName(), violationKey);

        if (currentViolations >= maxViolations) {
            getPlugin().getPunishmentManager().executePunishment(player, getDisplayName());
            data.resetViolationLevel(violationKey);
        }
    }
}
