package com.yourdomain.guardianac.managers;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PunishmentManager {

    private final GuardianAC plugin;

    public PunishmentManager(GuardianAC plugin) {
        this.plugin = plugin;
    }

    public void executePunishment(Player player, String checkName) {
        List<String> commands = plugin.getConfig().getStringList("punishments");

        // Run commands on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String command : commands) {
                String formattedCommand = command
                        .replace("%player%", player.getName())
                        .replace("%check%", checkName);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
            }
        });
    }

    public int getMaxViolations(String checkType, String checkName) {
        return plugin.getConfig().getInt("checks." + checkType + "." + checkName + ".max-violation", 20);
    }
}
