package com.yourdomain.guardianac.checks.combat.autoclicker;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * AutoClicker (F) — Jitter click analysis.
 * Detects suspiciously low standard deviation in click intervals
 * combined with elevated click speed, indicating machine-like precision.
 */
public class AutoClickerF extends Check {

    private static final int MIN_SAMPLES = 15;
    private static final double MAX_STD_DEV = 5.0;
    private static final double MIN_CPS = 14.0;

    public AutoClickerF(GuardianAC plugin) {
        super(plugin, "AutoClicker", "F", CheckType.COMBAT);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        List<Long> timestamps = data.getClickTimestamps();
        if (!timestamps.isEmpty()) {
            long interval = now - timestamps.get(timestamps.size() - 1);
            if (interval > 0 && interval < 2000) {
                data.addClickInterval(interval);
            }
        }
        data.addClickTimestamp(now);

        List<Long> intervals = data.getClickIntervals();
        if (intervals.size() < MIN_SAMPLES) return;

        double stdDev = MathUtil.standardDeviation(intervals);
        double mean = MathUtil.mean(intervals);
        if (mean <= 0) return;

        double cps = 1000.0 / mean;
        if (stdDev < MAX_STD_DEV && cps > MIN_CPS) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
        }
    }
}
