package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

/**
 * Detects impossible inventory click speed — clicking items in inventories
 * faster than humanly possible. Inventory bots (ChestStealer, InventoryManager)
 * can click items every single tick. This check enforces a minimum interval
 * between inventory clicks.
 */
public class InventoryClickCheck extends Check {

    private static final long MIN_CLICK_INTERVAL_MS = 30; // absolute minimum
    private static final int MAX_FAST_CLICKS = 6; // max fast clicks in window
    private static final long WINDOW_MS = 1000;

    public InventoryClickCheck(GuardianAC plugin) {
        super(plugin, "InventoryClick", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addInventoryClickTime(now);

        List<Long> clicks = data.getInventoryClickTimes();
        if (clicks.size() < 2) return;

        // Count fast clicks in window
        int fastClicks = 0;
        for (int i = clicks.size() - 1; i > 0; i--) {
            long interval = clicks.get(i) - clicks.get(i - 1);
            if (now - clicks.get(i) > WINDOW_MS) break;

            if (interval < MIN_CLICK_INTERVAL_MS) {
                fastClicks++;
            }
        }

        if (fastClicks >= MAX_FAST_CLICKS) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
            event.setCancelled(true);
        }
    }
}
