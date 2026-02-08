package com.yourdomain.guardianac.gui;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all in-game GUI menus for configuring GuardianAC.
 */
public class GuiManager {

    private final GuardianAC plugin;

    public GuiManager(GuardianAC plugin) {
        this.plugin = plugin;
    }

    // ═══════════════════════════════════════
    //  MAIN MENU
    // ═══════════════════════════════════════
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(
                new GuiHolder(GuiType.MAIN_MENU), 45,
                ChatUtils.colorize("&c&lGuardianAC &8- &7Main Menu"));

        // Fill border
        fillBorder(inv, 45);

        // Category buttons (row 2, slots 10-16)
        inv.setItem(10, createItem(Material.IRON_BOOTS, "&b&lMovement",
                "&7Fly, Speed, Scaffold",
                "",
                "&7Checks: &a" + countChecks(CheckType.MOVEMENT),
                "&7Enabled: &a" + countEnabled(CheckType.MOVEMENT) + "&7/&a" + countChecks(CheckType.MOVEMENT),
                "",
                "&eClick to configure"));

        inv.setItem(12, createItem(Material.DIAMOND_SWORD, "&c&lCombat",
                "&7KillAura, Reach, Velocity, AutoClicker",
                "",
                "&7Checks: &a" + countChecks(CheckType.COMBAT),
                "&7Enabled: &a" + countEnabled(CheckType.COMBAT) + "&7/&a" + countChecks(CheckType.COMBAT),
                "",
                "&eClick to configure"));

        inv.setItem(14, createItem(Material.GOLDEN_APPLE, "&6&lPlayer",
                "&7NoSlow, NoFall",
                "",
                "&7Checks: &a" + countChecks(CheckType.PLAYER),
                "&7Enabled: &a" + countEnabled(CheckType.PLAYER) + "&7/&a" + countChecks(CheckType.PLAYER),
                "",
                "&eClick to configure"));

        inv.setItem(16, createItem(Material.ENDER_PEARL, "&5&lExploit",
                "&7Timer, Phase, Jesus",
                "",
                "&7Checks: &a" + countChecks(CheckType.EXPLOIT),
                "&7Enabled: &a" + countEnabled(CheckType.EXPLOIT) + "&7/&a" + countChecks(CheckType.EXPLOIT),
                "",
                "&eClick to configure"));

        // Bottom row: info + settings + players
        inv.setItem(30, createItem(Material.BOOK, "&e&lPlugin Info",
                "&7Version: &c" + plugin.getDescription().getVersion(),
                "&7Total Checks: &a" + plugin.getCheckManager().getChecks().size(),
                "&7API: &aPaper 1.21.1",
                "",
                "&eClick for details"));

        inv.setItem(32, createItem(Material.COMPARATOR, "&d&lGlobal Settings",
                "&7Configure punishments, decay, prefix",
                "",
                "&eClick to configure"));

        inv.setItem(34, createItem(Material.PLAYER_HEAD, "&a&lOnline Players",
                "&7View violations per player",
                "&7Online: &a" + Bukkit.getOnlinePlayers().size(),
                "",
                "&eClick to view"));

        // Toggle all button
        long totalEnabled = plugin.getCheckManager().getChecks().stream().filter(Check::isEnabled).count();
        long totalChecks = plugin.getCheckManager().getChecks().size();
        boolean allEnabled = totalEnabled == totalChecks;
        inv.setItem(40, createItem(allEnabled ? Material.LIME_DYE : Material.RED_DYE,
                allEnabled ? "&a&lAll Checks Enabled" : "&c&lSome Checks Disabled",
                "&7Enabled: &a" + totalEnabled + "&7/&a" + totalChecks,
                "",
                "&eClick to toggle all " + (allEnabled ? "OFF" : "ON")));

        player.openInventory(inv);
    }

    // ═══════════════════════════════════════
    //  CATEGORY MENU
    // ═══════════════════════════════════════
    public void openCategoryMenu(Player player, CheckType category) {
        List<Check> checks = plugin.getCheckManager().getChecks().stream()
                .filter(c -> c.getCheckType() == category)
                .collect(Collectors.toList());

        int size = Math.max(27, (int) Math.ceil((checks.size() + 9) / 9.0) * 9);
        size = Math.min(54, size);

        Inventory inv = Bukkit.createInventory(
                new GuiHolder(GuiType.CATEGORY_MENU, category.getName()),
                size,
                ChatUtils.colorize("&c&lGuardianAC &8- &7" + category.getName()));

        fillBorder(inv, size);

        // Group checks by base name for display
        Map<String, List<Check>> grouped = new LinkedHashMap<>();
        for (Check check : checks) {
            grouped.computeIfAbsent(check.getName(), k -> new ArrayList<>()).add(check);
        }

        int slot = 10;
        for (Map.Entry<String, List<Check>> entry : grouped.entrySet()) {
            String baseName = entry.getKey();
            List<Check> subChecks = entry.getValue();

            long enabled = subChecks.stream().filter(Check::isEnabled).count();
            Material mat = getCheckMaterial(baseName);
            boolean allOn = enabled == subChecks.size();

            List<String> lore = new ArrayList<>();
            lore.add("&7Sub-checks: &a" + subChecks.size());
            lore.add("&7Enabled: &a" + enabled + "&7/&a" + subChecks.size());
            lore.add("");

            for (Check sub : subChecks) {
                String status = sub.isEnabled() ? "&a✔" : "&c✘";
                String subLabel = sub.getSubType() != null ? " (" + sub.getSubType() + ")" : "";
                lore.add("  " + status + " &7" + baseName + subLabel);
            }

            lore.add("");
            lore.add("&eLeft-click to toggle all " + baseName);
            lore.add("&eRight-click to configure individually");

            inv.setItem(slot, createItem(mat,
                    (allOn ? "&a" : "&c") + "&l" + baseName,
                    lore.toArray(new String[0])));

            slot++;
            if (slot % 9 == 8) slot += 2; // Skip borders
        }

        // Back button
        inv.setItem(size - 5, createItem(Material.ARROW, "&7&lBack",
                "&eClick to return to main menu"));

        player.openInventory(inv);
    }

    // ═══════════════════════════════════════
    //  CHECK SETTINGS MENU (individual sub-checks)
    // ═══════════════════════════════════════
    public void openCheckSettings(Player player, String baseName) {
        List<Check> subChecks = plugin.getCheckManager().getChecksByBaseName(baseName);
        if (subChecks.isEmpty()) return;

        int size = Math.max(27, (int) Math.ceil((subChecks.size() + 9) / 9.0) * 9);
        size = Math.min(54, size);

        Inventory inv = Bukkit.createInventory(
                new GuiHolder(GuiType.CHECK_SETTINGS, baseName),
                size,
                ChatUtils.colorize("&c&lGuardianAC &8- &7" + baseName));

        fillBorder(inv, size);

        int slot = 10;
        for (Check check : subChecks) {
            boolean on = check.isEnabled();
            String subLabel = check.getSubType() != null ? " (" + check.getSubType() + ")" : "";
            int maxVl = plugin.getPunishmentManager().getMaxViolations(check.getConfigKey());

            List<String> lore = new ArrayList<>();
            lore.add("&7Status: " + (on ? "&aEnabled" : "&cDisabled"));
            lore.add("&7Max Violations: &b" + maxVl);
            lore.add("&7Config Key: &8" + check.getConfigKey());
            lore.add("");

            // Show online player violations for this check
            int flagged = 0;
            for (Player online : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(online);
                if (data != null && data.getViolationLevel(check.getViolationKey()) > 0) {
                    lore.add("  &c" + online.getName() + " &8- &bx" + data.getViolationLevel(check.getViolationKey()));
                    flagged++;
                }
            }
            if (flagged == 0) {
                lore.add("  &7No active violations");
            }

            lore.add("");
            lore.add("&eLeft-click to toggle");
            lore.add("&eMiddle-click to reset violations");

            inv.setItem(slot, createItem(on ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                    (on ? "&a" : "&c") + "&l" + baseName + subLabel,
                    lore.toArray(new String[0])));

            slot++;
            if (slot % 9 == 8) slot += 2;
        }

        // Back button
        String categoryName = subChecks.get(0).getCheckType().getName();
        inv.setItem(size - 5, createItem(Material.ARROW, "&7&lBack",
                "&eClick to return to " + categoryName));

        player.openInventory(inv);
    }

    // ═══════════════════════════════════════
    //  GLOBAL SETTINGS MENU
    // ═══════════════════════════════════════
    public void openGlobalSettings(Player player) {
        Inventory inv = Bukkit.createInventory(
                new GuiHolder(GuiType.GLOBAL_SETTINGS), 27,
                ChatUtils.colorize("&c&lGuardianAC &8- &7Settings"));

        fillBorder(inv, 27);

        // Decay interval
        long decayInterval = plugin.getConfigManager().getViolationDecayIntervalMs() / 1000;
        inv.setItem(10, createItem(Material.CLOCK, "&e&lDecay Interval",
                "&7Current: &a" + decayInterval + "s",
                "",
                "&7How often violations decay.",
                "",
                "&eLeft-click: +5s",
                "&eRight-click: -5s"));

        // Decay after
        long decayAfter = plugin.getConfigManager().getViolationDecayAfterMs() / 1000;
        inv.setItem(12, createItem(Material.HOPPER, "&e&lDecay Delay",
                "&7Current: &a" + decayAfter + "s",
                "",
                "&7How long before violations start decaying.",
                "",
                "&eLeft-click: +10s",
                "&eRight-click: -10s"));

        // Prefix
        String prefix = plugin.getConfigManager().getPrefix();
        inv.setItem(14, createItem(Material.NAME_TAG, "&e&lPrefix",
                "&7Current: " + prefix,
                "",
                "&7The chat prefix for all messages."));

        // Punishment info
        List<String> punishments = plugin.getConfig().getStringList("punishments");
        List<String> punishLore = new ArrayList<>();
        punishLore.add("&7Current punishments:");
        for (String cmd : punishments) {
            punishLore.add("  &8- &c" + cmd);
        }
        punishLore.add("");
        punishLore.add("&7Edit in config.yml");
        inv.setItem(16, createItem(Material.IRON_AXE, "&c&lPunishments",
                punishLore.toArray(new String[0])));

        // Back button
        inv.setItem(22, createItem(Material.ARROW, "&7&lBack",
                "&eClick to return to main menu"));

        player.openInventory(inv);
    }

    // ═══════════════════════════════════════
    //  PLAYER LIST MENU
    // ═══════════════════════════════════════
    public void openPlayerList(Player viewer) {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        int size = Math.max(27, (int) Math.ceil((online.size() + 9) / 9.0) * 9);
        size = Math.min(54, size);

        Inventory inv = Bukkit.createInventory(
                new GuiHolder(GuiType.PLAYER_LIST), size,
                ChatUtils.colorize("&c&lGuardianAC &8- &7Players"));

        fillBorder(inv, size);

        int slot = 10;
        for (Player online1 : online) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(online1);

            List<String> lore = new ArrayList<>();
            lore.add("&7Ping: &e" + online1.getPing() + "ms");
            lore.add("");

            int totalVl = 0;
            if (data != null) {
                for (Check check : plugin.getCheckManager().getChecks()) {
                    int vl = data.getViolationLevel(check.getViolationKey());
                    if (vl > 0) {
                        lore.add("  &c" + check.getDisplayName() + " &8- &bx" + vl);
                        totalVl += vl;
                    }
                }
            }

            if (totalVl == 0) {
                lore.add("  &aNo violations");
            }

            lore.add("");
            lore.add("&7Total VL: &b" + totalVl);
            lore.add("");
            lore.add("&eClick to reset violations");

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(online1);
            meta.setDisplayName(ChatUtils.colorize((totalVl > 0 ? "&c" : "&a") + online1.getName()));
            meta.setLore(lore.stream().map(ChatUtils::colorize).collect(Collectors.toList()));
            head.setItemMeta(meta);

            inv.setItem(slot, head);
            slot++;
            if (slot % 9 == 8) slot += 2;
        }

        // Back button
        inv.setItem(size - 5, createItem(Material.ARROW, "&7&lBack",
                "&eClick to return to main menu"));

        viewer.openInventory(inv);
    }

    // ═══════════════════════════════════════
    //  UTILITY METHODS
    // ═══════════════════════════════════════

    private int countChecks(CheckType type) {
        return (int) plugin.getCheckManager().getChecks().stream()
                .filter(c -> c.getCheckType() == type).count();
    }

    private int countEnabled(CheckType type) {
        return (int) plugin.getCheckManager().getChecks().stream()
                .filter(c -> c.getCheckType() == type && c.isEnabled()).count();
    }

    private Material getCheckMaterial(String baseName) {
        return switch (baseName) {
            case "KillAura" -> Material.DIAMOND_SWORD;
            case "Reach" -> Material.FISHING_ROD;
            case "Velocity" -> Material.SLIME_BALL;
            case "AutoClicker" -> Material.STICK;
            case "Fly" -> Material.FEATHER;
            case "Speed" -> Material.SUGAR;
            case "Scaffold" -> Material.SCAFFOLDING;
            case "NoSlow" -> Material.COBWEB;
            case "NoFall" -> Material.LEATHER_BOOTS;
            case "Timer" -> Material.CLOCK;
            case "Phase" -> Material.ENDER_EYE;
            case "Jesus" -> Material.WATER_BUCKET;
            default -> Material.BARRIER;
        };
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize(name));
        if (lore.length > 0) {
            meta.setLore(Arrays.stream(lore).map(ChatUtils::colorize).collect(Collectors.toList()));
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inv, int size) {
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, "&r");
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
            }
        }
    }
}
