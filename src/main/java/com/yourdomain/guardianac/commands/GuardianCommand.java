package com.yourdomain.guardianac.commands;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GuardianCommand implements CommandExecutor {

    private final GuardianAC plugin;

    public GuardianCommand(GuardianAC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtils.colorize("&cGuardianAC &8» &7Version " + plugin.getDescription().getVersion()));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("guardian.admin")) {
                sender.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
                return true;
            }
            plugin.getConfigManager().loadConfig();
            sender.sendMessage(ChatUtils.colorize(plugin.getConfigManager().getPrefix() + "&aConfiguration reloaded."));
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (!sender.hasPermission("guardian.admin")) {
                sender.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatUtils.colorize("&cUsage: /guardian toggle <check>"));
                return true;
            }
            String checkName = args[1];
            Check check = plugin.getCheckManager().getCheckByName(checkName);
            if (check == null) {
                sender.sendMessage(ChatUtils.colorize("&cCheck '" + checkName + "' not found."));
                return true;
            }
            check.setEnabled(!check.isEnabled());
            sender.sendMessage(ChatUtils.colorize(plugin.getConfigManager().getPrefix() + "&7Check &c" + check.getName() + " &7has been " + (check.isEnabled() ? "&aenabled" : "&cdisabled") + "&7."));
            return true;
        }

        sender.sendMessage(ChatUtils.colorize("&cUnknown subcommand. Use /guardian [reload|toggle]"));
        return true;
    }
}
