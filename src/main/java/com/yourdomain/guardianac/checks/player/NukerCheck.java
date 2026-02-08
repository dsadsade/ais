package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Detects Nuker hacks — breaking many blocks in different positions simultaneously.
 * Nuker mods break blocks in a sphere/cube around the player at inhuman rates.
 * Checks for breaking blocks at distant positions from each other in rapid succession.
 */
public class NukerCheck extends Check {

    private static final int MAX_BREAKS_PER_WINDOW = 8;
    private static final long NUKER_WINDOW_MS = 1000; // 1 second
    private static final double MAX_BREAK_DISTANCE = 6.0; // max distance between consecutive breaks

    public NukerCheck(GuardianAC plugin) {
        super(plugin, "Nuker", CheckType.PLAYER);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        Location breakLoc = event.getBlock().getLocation();

        // Reset window if expired
        if (now - data.getNukerWindowStart() > NUKER_WINDOW_MS) {
            data.resetNukerBreakCount();
            data.setNukerWindowStart(now);
        }

        data.incrementNukerBreakCount();

        // Check if the break locations are suspiciously spread out
        Location lastBreak = data.getLastBreakLocation();
        if (lastBreak != null && lastBreak.getWorld() == breakLoc.getWorld()) {
            double dist = lastBreak.distance(breakLoc);

            // Breaking blocks far apart in rapid succession = Nuker
            if (dist > MAX_BREAK_DISTANCE && data.getNukerBreakCount() > 3) {
                handleViolation(data, player, 6.0, 10.0, 1.0);
            }
        }

        // Too many breaks in the window
        if (data.getNukerBreakCount() >= MAX_BREAKS_PER_WINDOW) {
            handleViolation(data, player, 5.0, 10.0, 1.5);
            data.resetNukerBreakCount();
            data.setNukerWindowStart(now);
        }

        data.setLastBreakLocation(breakLoc);
    }
}
