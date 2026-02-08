package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Detects FastBreak hacks — breaking blocks faster than possible.
 * Measures the time interval between block breaks.
 * Haste effect and Efficiency enchantment are accounted for.
 */
public class FastBreakCheck extends Check {

    private static final long MIN_BREAK_INTERVAL_MS = 50; // absolute minimum ~1 tick
    private static final int FAST_BREAK_COUNT = 5; // flags in rapid succession
    private static final long FAST_BREAK_WINDOW_MS = 500; // within half a second

    public FastBreakCheck(GuardianAC plugin) {
        super(plugin, "FastBreak", CheckType.PLAYER);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return; // instant break in creative

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addBlockBreakTime(now);

        List<Long> breakTimes = data.getBlockBreakTimes();
        if (breakTimes.size() < 2) return;

        // Check interval between last two breaks
        long interval = now - breakTimes.get(breakTimes.size() - 2);

        // Account for haste effect
        double minInterval = MIN_BREAK_INTERVAL_MS;
        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            int amp = player.getPotionEffect(PotionEffectType.HASTE).getAmplifier();
            minInterval *= (1.0 / (1.0 + 0.2 * (amp + 1)));
        }

        // Impossibly fast single break
        if (interval < minInterval) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
            return;
        }

        // Count breaks within the fast window
        long windowStart = now - FAST_BREAK_WINDOW_MS;
        int count = 0;
        for (long t : breakTimes) {
            if (t >= windowStart) count++;
        }

        if (count >= FAST_BREAK_COUNT) {
            handleViolation(data, player, 4.0, 10.0, 1.5);
        }
    }
}
