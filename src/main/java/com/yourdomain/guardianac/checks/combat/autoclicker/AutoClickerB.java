package com.yourdomain.guardianac.checks.combat.autoclicker;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * AutoClicker (B) — Click pattern consistency / regularity check.
 * Analyzes the standard deviation and kurtosis of click intervals.
 * Human clicking has natural variation; autoclickers produce unnaturally
 * consistent intervals with flat (platykurtic) distributions.
 */
public class AutoClickerB extends Check {

    private static final int MIN_INTERVALS = 15;

    public AutoClickerB(GuardianAC plugin) {
        super(plugin, "AutoClicker", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();

        // Track interval between clicks
        List<Long> timestamps = data.getClickTimestamps();
        if (!timestamps.isEmpty()) {
            long lastClick = timestamps.get(timestamps.size() - 1);
            long interval = now - lastClick;
            if (interval > 10 && interval < 2000) { // Filter out absurd values
                data.addClickInterval(interval);
            }
        }
        data.addClickTimestamp(now);

        List<Long> intervals = data.getClickIntervals();
        if (intervals.size() < MIN_INTERVALS) return;

        double stdDev = MathUtil.standardDeviation(intervals);
        double mean = intervals.stream().mapToDouble(Long::doubleValue).average().orElse(0);
        double kurtosis = MathUtil.kurtosis(intervals);

        if (mean <= 0) return;

        double cv = stdDev / mean; // coefficient of variation

        // Check 1: Very low CV (consistent intervals)
        if (cv < 0.03 && intervals.size() >= 20) {
            handleViolation(data, player, 3.0, 7.0, 0.5);
        } else if (cv < 0.06 && intervals.size() >= 25) {
            handleViolation(data, player, 1.5, 7.0, 0.5);
        }

        // Check 2: Negative kurtosis (flat distribution — all intervals nearly same)
        if (kurtosis < -1.5 && intervals.size() >= 20) {
            handleViolation(data, player, 2.0, 7.0, 0.5);
        }

        // Check 3: Perfect patterns — zero outliers with enough data
        if (intervals.size() >= 25) {
            int outliers = MathUtil.countOutliers(intervals);
            if (outliers == 0 && stdDev < 5.0) {
                handleViolation(data, player, 2.5, 7.0, 0.5);
            }
        }
    }
}
