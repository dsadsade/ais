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
 * Detects AutoTotem hacks — instantly moving a totem of undying to the offhand
 * after popping one. Legitimate players take time to find and move the totem.
 * AutoTotem mods swap within 1-2 ticks, which is humanly impossible in combat.
 */
public class AutoTotemCheck extends Check {

    private static final long MIN_SWAP_INTERVAL_MS = 100; // minimum time between totem swaps
    private static final int FAST_SWAPS_THRESHOLD = 2;
    private static final long FAST_SWAP_WINDOW_MS = 1000;

    public AutoTotemCheck(GuardianAC plugin) {
        super(plugin, "AutoTotem", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (isExempt(player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack clicking = event.getCurrentItem();

        boolean isTotemSwap = false;

        // Check if player is placing a totem into the offhand slot (slot 45)
        if (event.getRawSlot() == 45 || event.getSlot() == 40) {
            if ((cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING)
                    || (clicking != null && clicking.getType() == Material.TOTEM_OF_UNDYING)) {
                isTotemSwap = true;
            }
        }

        // Check shift-click of totem (goes to offhand if empty)
        if (event.isShiftClick() && clicking != null && clicking.getType() == Material.TOTEM_OF_UNDYING) {
            isTotemSwap = true;
        }

        if (!isTotemSwap) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addTotemSwapTime(now);

        List<Long> swapTimes = data.getTotemSwapTimes();
        if (swapTimes.size() < 2) return;

        long interval = now - swapTimes.get(swapTimes.size() - 2);
        if (interval < MIN_SWAP_INTERVAL_MS) {
            handleViolation(data, player, 6.0, 8.0, 0.5);
            return;
        }

        // Multiple rapid swaps
        long windowStart = now - FAST_SWAP_WINDOW_MS;
        int count = 0;
        for (long t : swapTimes) {
            if (t >= windowStart) count++;
        }

        if (count >= FAST_SWAPS_THRESHOLD) {
            handleViolation(data, player, 4.0, 8.0, 1.0);
        }
    }
}
