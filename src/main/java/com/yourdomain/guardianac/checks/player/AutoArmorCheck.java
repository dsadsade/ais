package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Detects AutoArmor hacks — automatically equipping armor from inventory impossibly fast.
 * Legitimate players take time to drag armor pieces into their slots.
 * AutoArmor mods click all 4 armor pieces into slots within milliseconds.
 */
public class AutoArmorCheck extends Check {

    private static final long MIN_EQUIP_INTERVAL_MS = 50; // too fast between clicks
    private static final int FAST_EQUIPS_THRESHOLD = 3;
    private static final long FAST_EQUIP_WINDOW_MS = 500; // 3+ armor equips in 500ms

    public AutoArmorCheck(GuardianAC plugin) {
        super(plugin, "AutoArmor", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (isExempt(player)) return;

        // Only check player inventory / crafting view
        if (event.getInventory().getType() != InventoryType.CRAFTING
                && event.getInventory().getType() != InventoryType.PLAYER) return;

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        // Check if the clicked item is armor
        if (!isArmor(item.getType())) return;

        // Check if moving into an armor slot
        int slot = event.getRawSlot();
        boolean isArmorSlot = (slot >= 5 && slot <= 8); // armor slots in player inventory

        if (!isArmorSlot && !event.isShiftClick()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addArmorEquipTime(now);

        List<Long> equipTimes = data.getArmorEquipTimes();
        if (equipTimes.size() < 2) return;

        // Check interval
        long interval = now - equipTimes.get(equipTimes.size() - 2);
        if (interval < MIN_EQUIP_INTERVAL_MS) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
            return;
        }

        // Count equips in window
        long windowStart = now - FAST_EQUIP_WINDOW_MS;
        int count = 0;
        for (long t : equipTimes) {
            if (t >= windowStart) count++;
        }

        if (count >= FAST_EQUIPS_THRESHOLD) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
        }
    }

    private boolean isArmor(Material mat) {
        String name = mat.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || mat == Material.ELYTRA || mat == Material.TURTLE_HELMET;
    }
}
