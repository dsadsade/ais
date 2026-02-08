package com.yourdomain.guardianac.managers;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.commands.GuardianCommand;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class AlertManager {

    private final GuardianAC plugin;

    public AlertManager(GuardianAC plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(Player player, PlayerData playerData, String displayName, String violationKey) {
        if (player == null || !player.isOnline()) return;

        int vl = playerData.getViolationLevel(violationKey);
        String prefix = plugin.getConfigManager().getPrefix();
        String message = prefix + "&c" + player.getName() + " &7failed &c" + displayName
                + " &8[&bx" + vl + "&8]";

        // Get which players have alerts enabled
        GuardianCommand cmd = (GuardianCommand) plugin.getCommand("guardian").getExecutor();
        Set<UUID> alertsEnabled = cmd.getAlertsEnabled();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("guardian.alerts")) {
                // If the alerts set is empty, everyone with permission gets alerts by default
                // Once someone toggles, only those in the set get them
                if (alertsEnabled.isEmpty() || alertsEnabled.contains(onlinePlayer.getUniqueId())) {
                    // Use Adventure API for hover text with more details
                    String hoverText = "&7Player: &c" + player.getName() + "\n"
                            + "&7Check: &c" + displayName + "\n"
                            + "&7Violations: &b" + vl + "\n"
                            + "&7Ping: &e" + player.getPing() + "ms\n"
                            + "&7Location: &c" + formatLocation(player);

                    Component messageComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(message)
                            .hoverEvent(HoverEvent.showText(
                                    LegacyComponentSerializer.legacyAmpersand().deserialize(hoverText)));

                    onlinePlayer.sendMessage(messageComponent);
                }
            }
        }

        // Also log to console
        plugin.getLogger().warning("[ALERT] " + player.getName() + " failed " + displayName
                + " (x" + vl + ") at " + formatLocation(player));
    }

    /** @deprecated Use sendAlert with violationKey parameter */
    @Deprecated
    public void sendAlert(Player player, PlayerData playerData, String checkName) {
        sendAlert(player, playerData, checkName, checkName);
    }

    private String formatLocation(Player player) {
        return String.format("%.1f, %.1f, %.1f (%s)",
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                player.getWorld().getName());
    }
}
