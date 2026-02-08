package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.List;

/**
 * Detects FastProjectile hacks — throwing projectiles (snowballs, eggs, ender pearls,
 * splash potions) faster than humanly possible.
 * Vanilla has a ~300ms cooldown between throws. Fast projectile mods bypass this delay.
 */
public class FastProjectileCheck extends Check {

    private static final long MIN_THROW_INTERVAL_MS = 200; // absolute minimum
    private static final int MAX_THROWS_PER_SECOND = 6;
    private static final long WINDOW_MS = 1000;

    public FastProjectileCheck(GuardianAC plugin) {
        super(plugin, "FastProjectile", CheckType.COMBAT);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addProjectileThrowTime(now);

        List<Long> throwTimes = data.getProjectileThrowTimes();
        if (throwTimes.size() < 2) return;

        // Check interval between last two throws
        long interval = now - throwTimes.get(throwTimes.size() - 2);
        if (interval < MIN_THROW_INTERVAL_MS) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
            event.setCancelled(true);
            return;
        }

        // Count throws within window
        long windowStart = now - WINDOW_MS;
        int count = 0;
        for (long t : throwTimes) {
            if (t >= windowStart) count++;
        }

        if (count > MAX_THROWS_PER_SECOND) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
            event.setCancelled(true);
        }
    }
}
