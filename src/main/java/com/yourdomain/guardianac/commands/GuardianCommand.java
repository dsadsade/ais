package com.yourdomain.guardianac.commands;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GuardianCommand implements CommandExecutor {

    private final GuardianAC plugin;
    private final Set<UUID> alertsEnabled;

    public GuardianCommand(GuardianAC plugin) {
        this.plugin = plugin;
        this.alertsEnabled = new HashSet<>();
    }

    public Set<UUID> getAlertsEnabled() {
        return alertsEnabled;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String prefix = plugin.getConfigManager().getPrefix();

        if (args.length == 0) {
            // No args — if player, open GUI; if console, show help
            if (sender instanceof Player player) {
                if (!player.hasPermission("guardian.admin")) {
                    player.sendMessage(ChatUtils.colorize("&cYou do not have permission."));
                    return true;
                }
                plugin.getGuiManager().openMainMenu(player);
            } else {
                showHelp(sender, prefix);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui", "menu" -> handleGui(sender, prefix);
            case "reload" -> handleReload(sender, prefix);
            case "toggle" -> handleToggle(sender, args, prefix);
            case "alerts" -> handleAlerts(sender, prefix);
            case "status" -> handleStatus(sender, prefix);
            case "reset" -> handleReset(sender, args, prefix);
            case "info" -> handleInfo(sender, args, prefix);
            case "help" -> showHelp(sender, prefix);
            default -> sender.sendMessage(ChatUtils.colorize(prefix + "&cUnknown subcommand. Use &7/guardian help"));
        }

        return true;
    }

    private void showHelp(CommandSender sender, String prefix) {
        sender.sendMessage(ChatUtils.colorize("&8&m─────────────────────────────────────"));
        sender.sendMessage(ChatUtils.colorize("  &c&lGuardianAC &8- &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(ChatUtils.colorize("  &7AntiCheat for Minecraft 1.21.1"));
        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian &8- &7Open control panel (GUI)"));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian gui &8- &7Open control panel (GUI)"));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian reload &8- &7Reload configuration"));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian toggle <check> &8- &7Toggle a check"));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian alerts &8- &7Toggle alert messages"));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian status &8- &7View check statuses"));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian reset <player> &8- &7Reset violations"));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian info <check> &8- &7Check details"));
        sender.sendMessage(ChatUtils.colorize("  &c/guardian help &8- &7Show this help message"));
        sender.sendMessage(ChatUtils.colorize("&8&m─────────────────────────────────────"));
    }

    private void handleGui(CommandSender sender, String prefix) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cOnly players can open the GUI."));
            return;
        }
        if (!player.hasPermission("guardian.admin")) {
            player.sendMessage(ChatUtils.colorize("&cYou do not have permission."));
            return;
        }
        plugin.getGuiManager().openMainMenu(player);
    }

    private void handleReload(CommandSender sender, String prefix) {
        if (!sender.hasPermission("guardian.admin")) {
            sender.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
            return;
        }
        plugin.getConfigManager().loadConfig();
        // Refresh check enabled states from config
        for (Check check : plugin.getCheckManager().getChecks()) {
            check.setEnabled(plugin.getConfig().getBoolean("checks." + check.getConfigKey() + ".enabled", true));
        }
        // Restart decay task with possibly new settings
        plugin.startDecayTask();
        sender.sendMessage(ChatUtils.colorize(prefix + "&aConfiguration reloaded successfully."));
    }

    private void handleToggle(CommandSender sender, String[] args, String prefix) {
        if (!sender.hasPermission("guardian.admin")) {
            sender.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cUsage: /guardian toggle <check>"));
            return;
        }
        Check check = plugin.getCheckManager().getCheckByName(args[1]);
        if (check == null) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cCheck '&7" + args[1] + "&c' not found."));
            return;
        }
        check.setEnabled(!check.isEnabled());
        sender.sendMessage(ChatUtils.colorize(prefix + "&7Check &c" + check.getDisplayName() + " &7has been "
                + (check.isEnabled() ? "&aenabled" : "&cdisabled") + "&7."));
    }

    private void handleAlerts(CommandSender sender, String prefix) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cOnly players can toggle alerts."));
            return;
        }
        if (!player.hasPermission("guardian.alerts")) {
            player.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
            return;
        }

        if (alertsEnabled.contains(player.getUniqueId())) {
            alertsEnabled.remove(player.getUniqueId());
            player.sendMessage(ChatUtils.colorize(prefix + "&7Alerts have been &cdisabled&7."));
        } else {
            alertsEnabled.add(player.getUniqueId());
            player.sendMessage(ChatUtils.colorize(prefix + "&7Alerts have been &aenabled&7."));
        }
    }

    private void handleStatus(CommandSender sender, String prefix) {
        if (!sender.hasPermission("guardian.admin")) {
            sender.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
            return;
        }
        sender.sendMessage(ChatUtils.colorize("&8&m─────────────────────────────────────"));
        sender.sendMessage(ChatUtils.colorize("  &c&lGuardianAC &8- &7Check Status"));
        sender.sendMessage(ChatUtils.colorize(""));

        for (Check check : plugin.getCheckManager().getChecks()) {
            String status = check.isEnabled() ? "&a✔ Enabled" : "&c✘ Disabled";
            sender.sendMessage(ChatUtils.colorize("  &7" + check.getDisplayName() + " &8- " + status));
        }

        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize("  &7Total: &c" + plugin.getCheckManager().getChecks().size() + " checks"));
        sender.sendMessage(ChatUtils.colorize("&8&m─────────────────────────────────────"));
    }

    private void handleReset(CommandSender sender, String[] args, String prefix) {
        if (!sender.hasPermission("guardian.admin")) {
            sender.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cUsage: /guardian reset <player>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cPlayer '&7" + args[1] + "&c' not found or not online."));
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        if (data == null) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cNo data found for that player."));
            return;
        }
        // Reset all violation levels
        for (Check check : plugin.getCheckManager().getChecks()) {
            data.resetViolationLevel(check.getViolationKey());
        }
        sender.sendMessage(ChatUtils.colorize(prefix + "&aAll violations for &c" + target.getName() + " &ahave been reset."));
    }

    private void handleInfo(CommandSender sender, String[] args, String prefix) {
        if (!sender.hasPermission("guardian.admin")) {
            sender.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cUsage: /guardian info <check>"));
            return;
        }
        Check check = plugin.getCheckManager().getCheckByName(args[1]);
        if (check == null) {
            sender.sendMessage(ChatUtils.colorize(prefix + "&cCheck '&7" + args[1] + "&c' not found."));
            return;
        }  
        sender.sendMessage(ChatUtils.colorize("&8&m─────────────────────────────────────"));
        sender.sendMessage(ChatUtils.colorize("  &c&l" + check.getDisplayName()));
        sender.sendMessage(ChatUtils.colorize("  &7Status: " + (check.isEnabled() ? "&aEnabled" : "&cDisabled")));
        sender.sendMessage(ChatUtils.colorize("  &7Type: &c" + check.getCheckType().getName()));

        // Show violations for online players
        int flaggedPlayers = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(online);
            if (data != null && data.getViolationLevel(check.getViolationKey()) > 0) {
                flaggedPlayers++;
                sender.sendMessage(ChatUtils.colorize("  &7  - &c" + online.getName() + " &8(&bx" + data.getViolationLevel(check.getViolationKey()) + "&8)"));
            }
        }
        if (flaggedPlayers == 0) {
            sender.sendMessage(ChatUtils.colorize("  &7  No flagged players."));
        }
        sender.sendMessage(ChatUtils.colorize("&8&m─────────────────────────────────────"));
    }
}
