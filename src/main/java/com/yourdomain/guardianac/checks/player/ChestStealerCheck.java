package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

/**
 * Detects ChestStealer hacks — looting items from chests impossibly fast.
 * Legitimate players take time to find and click items in inventories.
 * ChestStealer mods click all items within milliseconds of opening a container.
 */
public class ChestStealerCheck extends Check {

    private static final long MIN_CLICK_INTERVAL_MS = 30; // absolute minimum between clicks
    private static final int FAST_CLICK_COUNT = 6;
    private static final long FAST_CLICK_WINDOW_MS = 400; // 6 clicks in 400ms is inhuman

    public ChestStealerCheck(GuardianAC plugin) {
        super(plugin, "ChestStealer", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (isExempt(player)) return;

        // Only check container inventories, not the player's own inventory
        InventoryType type = event.getInventory().getType();
        if (type != InventoryType.CHEST && type != InventoryType.ENDER_CHEST
                && type != InventoryType.BARREL && type != InventoryType.SHULKER_BOX
                && type != InventoryType.HOPPER && type != InventoryType.DROPPER
                && type != InventoryType.DISPENSER) {
            return;
        }

        // Only count clicks on the top inventory (the container, not player's inventory)
        if (event.getRawSlot() >= event.getInventory().getSize()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addChestClickTime(now);

        List<Long> clickTimes = data.getChestClickTimes();
        if (clickTimes.size() < 2) return;

        // Check interval between last two clicks
        long interval = now - clickTimes.get(clickTimes.size() - 2);
        if (interval < MIN_CLICK_INTERVAL_MS) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
            return;
        }

        // Count clicks within the fast window
        long windowStart = now - FAST_CLICK_WINDOW_MS;
        int count = 0;
        for (long t : clickTimes) {
            if (t >= windowStart) count++;
        }

        if (count >= FAST_CLICK_COUNT) {
            handleViolation(data, player, 5.0, 12.0, 1.0);
        }
    }
}
