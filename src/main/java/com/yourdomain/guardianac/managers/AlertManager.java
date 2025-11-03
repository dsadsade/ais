package com.yourdomain.guardianac.managers;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AlertManager {

    private final GuardianAC plugin;

    public AlertManager(GuardianAC plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(Player player, PlayerData playerData, String checkName) {
        String message = plugin.getConfigManager().getPrefix() +
                "&c" + player.getName() + " &7failed &c" + checkName +
                " &7(&bx" + playerData.getViolationLevel(checkName) + "&7)";

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("guardian.alerts")) {
                onlinePlayer.sendMessage(ChatUtils.colorize(message));
            }
        }
    }
}
