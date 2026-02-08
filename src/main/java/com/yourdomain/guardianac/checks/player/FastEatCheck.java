package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Detects FastEat hacks — consuming food/potions faster than possible.
 * Vanilla food consumption takes 32 ticks (1.6 seconds).
 * Vanilla potion drinking takes 32 ticks (1.6 seconds).
 * Dried kelp takes 16 ticks (0.8 seconds).
 */
public class FastEatCheck extends Check {

    private static final long MIN_EAT_TIME_MS = 1200; // slightly less than 1.6s for tolerance
    private static final long FAST_EAT_TIME_MS = 500;  // obviously too fast

    public FastEatCheck(GuardianAC plugin) {
        super(plugin, "FastEat", CheckType.PLAYER);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long startTime = data.getItemUseStartTime();
        if (startTime <= 0) return;

        long elapsed = System.currentTimeMillis() - startTime;
        ItemStack item = event.getItem();

        // Determine minimum time based on item type
        long minTime = MIN_EAT_TIME_MS;
        if (item.getType() == Material.DRIED_KELP) {
            minTime = 500; // kelp is faster to eat
        }

        if (elapsed < FAST_EAT_TIME_MS) {
            // Impossibly fast — instant eat
            handleViolation(data, player, 8.0, 10.0, 0.5);
        } else if (elapsed < minTime) {
            double severity = (minTime - elapsed) / 100.0;
            handleViolation(data, player, severity, 10.0, 1.0);
        }
    }
}
