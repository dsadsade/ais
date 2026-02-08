package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

/**
 * Detects FastPlace hacks — placing blocks faster than physically possible.
 * Vanilla block placement has a minimum delay of ~50ms (1 tick) between placements.
 * FastPlace hacks bypass this delay to place many blocks per tick.
 */
public class FastPlaceCheck extends Check {

    private static final long MIN_PLACE_INTERVAL_MS = 40; // just under 1 tick
    private static final int FAST_PLACE_COUNT = 6;
    private static final long FAST_PLACE_WINDOW_MS = 500;

    public FastPlaceCheck(GuardianAC plugin) {
        super(plugin, "FastPlace", CheckType.PLAYER);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addBlockPlaceTime(now);

        List<Long> placeTimes = data.getBlockPlaceTimes();
        if (placeTimes.size() < 2) return;

        // Check interval between last two placements
        long interval = now - placeTimes.get(placeTimes.size() - 2);

        if (interval < MIN_PLACE_INTERVAL_MS) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
            return;
        }

        // Count placements within the fast window
        long windowStart = now - FAST_PLACE_WINDOW_MS;
        int count = 0;
        for (long t : placeTimes) {
            if (t >= windowStart) count++;
        }

        if (count >= FAST_PLACE_COUNT) {
            handleViolation(data, player, 4.0, 10.0, 1.5);
        }
    }
}
