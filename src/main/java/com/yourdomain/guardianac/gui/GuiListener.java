package com.yourdomain.guardianac.gui;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all click events inside GuardianAC GUI inventories.
 */
public class GuiListener implements Listener {

    private final GuardianAC plugin;

    public GuiListener(GuardianAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GuiHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        int slot = event.getRawSlot();

        switch (holder.getGuiType()) {
            case MAIN_MENU -> handleMainMenu(player, slot, event.getClick());
            case CATEGORY_MENU -> handleCategoryMenu(player, slot, event.getClick(), holder.getMetadata());
            case CHECK_SETTINGS -> handleCheckSettings(player, slot, event.getClick(), holder.getMetadata());
            case GLOBAL_SETTINGS -> handleGlobalSettings(player, slot, event.getClick());
            case PLAYER_LIST -> handlePlayerList(player, slot, event.getClick());
        }
    }

    // ═══════════════════════════════════════
    //  MAIN MENU HANDLER
    // ═══════════════════════════════════════
    private void handleMainMenu(Player player, int slot, ClickType click) {
        switch (slot) {
            case 10 -> plugin.getGuiManager().openCategoryMenu(player, CheckType.MOVEMENT);
            case 12 -> plugin.getGuiManager().openCategoryMenu(player, CheckType.COMBAT);
            case 14 -> plugin.getGuiManager().openCategoryMenu(player, CheckType.PLAYER);
            case 16 -> plugin.getGuiManager().openCategoryMenu(player, CheckType.EXPLOIT);
            case 30 -> {
                // Plugin info — just show message
                player.closeInventory();
                String prefix = plugin.getConfigManager().getPrefix();
                player.sendMessage(ChatUtils.colorize(prefix + "&7GuardianAC v&c" + plugin.getDescription().getVersion()));
                player.sendMessage(ChatUtils.colorize(prefix + "&7Checks: &a" + plugin.getCheckManager().getChecks().size()));
                player.sendMessage(ChatUtils.colorize(prefix + "&7Target: &aPaper 1.21.1"));
            }
            case 32 -> plugin.getGuiManager().openGlobalSettings(player);
            case 34 -> plugin.getGuiManager().openPlayerList(player);
            case 40 -> {
                // Toggle all checks on/off
                long enabled = plugin.getCheckManager().getChecks().stream().filter(Check::isEnabled).count();
                boolean allOn = enabled == plugin.getCheckManager().getChecks().size();
                boolean newState = !allOn;
                for (Check check : plugin.getCheckManager().getChecks()) {
                    check.setEnabled(newState);
                    plugin.getConfig().set("checks." + check.getConfigKey() + ".enabled", newState);
                }
                plugin.saveConfig();
                player.sendMessage(ChatUtils.colorize(plugin.getConfigManager().getPrefix()
                        + (newState ? "&aAll checks enabled." : "&cAll checks disabled.")));
                plugin.getGuiManager().openMainMenu(player);
            }
        }
    }

    // ═══════════════════════════════════════
    //  CATEGORY MENU HANDLER
    // ═══════════════════════════════════════
    private void handleCategoryMenu(Player player, int slot, ClickType click, String categoryName) {
        CheckType category = CheckType.valueOf(categoryName.toUpperCase());
        int size = player.getOpenInventory().getTopInventory().getSize();

        // Back button
        if (slot == size - 5) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }

        // Calculate which check group was clicked
        List<String> baseNames = plugin.getCheckManager().getChecks().stream()
                .filter(c -> c.getCheckType() == category)
                .map(Check::getName)
                .distinct()
                .collect(Collectors.toList());

        int index = slotToIndex(slot);
        if (index < 0 || index >= baseNames.size()) return;
        String baseName = baseNames.get(index);

        if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
            // Right click → open individual check settings
            plugin.getGuiManager().openCheckSettings(player, baseName);
        } else {
            // Left click → toggle all sub-checks of this type
            List<Check> subChecks = plugin.getCheckManager().getChecksByBaseName(baseName);
            long enabled = subChecks.stream().filter(Check::isEnabled).count();
            boolean newState = enabled != subChecks.size();
            for (Check check : subChecks) {
                check.setEnabled(newState);
                plugin.getConfig().set("checks." + check.getConfigKey() + ".enabled", newState);
            }
            plugin.saveConfig();
            player.sendMessage(ChatUtils.colorize(plugin.getConfigManager().getPrefix()
                    + "&7" + baseName + " checks " + (newState ? "&aenabled" : "&cdisabled") + "&7."));
            plugin.getGuiManager().openCategoryMenu(player, category);
        }
    }

    // ═══════════════════════════════════════
    //  CHECK SETTINGS HANDLER
    // ═══════════════════════════════════════
    private void handleCheckSettings(Player player, int slot, ClickType click, String baseName) {
        List<Check> subChecks = plugin.getCheckManager().getChecksByBaseName(baseName);
        int size = player.getOpenInventory().getTopInventory().getSize();

        // Back button
        if (slot == size - 5) {
            if (!subChecks.isEmpty()) {
                plugin.getGuiManager().openCategoryMenu(player, subChecks.get(0).getCheckType());
            }
            return;
        }

        int index = slotToIndex(slot);
        if (index < 0 || index >= subChecks.size()) return;
        Check check = subChecks.get(index);

        if (click == ClickType.MIDDLE) {
            // Middle click → reset violations for all players
            for (Player online : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(online);
                if (data != null) {
                    data.resetViolationLevel(check.getViolationKey());
                }
            }
            player.sendMessage(ChatUtils.colorize(plugin.getConfigManager().getPrefix()
                    + "&aViolations reset for " + check.getDisplayName()));
            plugin.getGuiManager().openCheckSettings(player, baseName);
        } else {
            // Left/right click → toggle
            check.setEnabled(!check.isEnabled());
            plugin.getConfig().set("checks." + check.getConfigKey() + ".enabled", check.isEnabled());
            plugin.saveConfig();
            player.sendMessage(ChatUtils.colorize(plugin.getConfigManager().getPrefix()
                    + "&7" + check.getDisplayName() + " "
                    + (check.isEnabled() ? "&aenabled" : "&cdisabled") + "&7."));
            plugin.getGuiManager().openCheckSettings(player, baseName);
        }
    }

    // ═══════════════════════════════════════
    //  GLOBAL SETTINGS HANDLER
    // ═══════════════════════════════════════
    private void handleGlobalSettings(Player player, int slot, ClickType click) {
        FileConfiguration config = plugin.getConfig();
        String prefix = plugin.getConfigManager().getPrefix();

        switch (slot) {
            case 10 -> {
                // Decay interval
                long current = config.getLong("violation-decay.interval-seconds", 30);
                if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
                    current = Math.min(300, current + 5);
                } else if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
                    current = Math.max(5, current - 5);
                }
                config.set("violation-decay.interval-seconds", current);
                plugin.saveConfig();
                player.sendMessage(ChatUtils.colorize(prefix + "&7Decay interval set to &a" + current + "s"));
                plugin.getGuiManager().openGlobalSettings(player);
            }
            case 12 -> {
                // Decay after
                long current = config.getLong("violation-decay.decay-after-seconds", 60);
                if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
                    current = Math.min(600, current + 10);
                } else if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
                    current = Math.max(10, current - 10);
                }
                config.set("violation-decay.decay-after-seconds", current);
                plugin.saveConfig();
                player.sendMessage(ChatUtils.colorize(prefix + "&7Decay delay set to &a" + current + "s"));
                plugin.getGuiManager().openGlobalSettings(player);
            }
            case 22 -> {
                // Back
                plugin.getGuiManager().openMainMenu(player);
            }
        }
    }

    // ═══════════════════════════════════════
    //  PLAYER LIST HANDLER
    // ═══════════════════════════════════════
    private void handlePlayerList(Player player, int slot, ClickType click) {
        int size = player.getOpenInventory().getTopInventory().getSize();

        // Back button
        if (slot == size - 5) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }

        int index = slotToIndex(slot);
        if (index < 0) return;

        List<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers().stream().toList();
        if (index >= onlinePlayers.size()) return;

        Player target = onlinePlayers.get(index);
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        if (data != null) {
            for (Check check : plugin.getCheckManager().getChecks()) {
                data.resetViolationLevel(check.getViolationKey());
            }
            player.sendMessage(ChatUtils.colorize(plugin.getConfigManager().getPrefix()
                    + "&aAll violations reset for &c" + target.getName()));
        }
        plugin.getGuiManager().openPlayerList(player);
    }

    // ═══════════════════════════════════════
    //  UTILITY
    // ═══════════════════════════════════════
    /**
     * Converts a raw inventory slot to a content index (skipping borders).
     * Border layout: first row (0-8), last row, and columns 0 and 8.
     * Content starts at slot 10, skipping col 0 and 8.
     */
    private int slotToIndex(int slot) {
        if (slot < 10) return -1;
        int row = slot / 9;
        int col = slot % 9;
        if (col == 0 || col == 8) return -1;

        // Content slots: row 1+ col 1-7
        return (row - 1) * 7 + (col - 1);
    }
}
