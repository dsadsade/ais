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
 * AutoClicker (G) — Click skewness analysis.
 * Auto-clickers produce symmetric distributions, while human clicking
 * has positive skewness (more short intervals with occasional delays).
 */
public class AutoClickerG extends Check {

    private static final int MIN_SAMPLES = 20;

    public AutoClickerG(GuardianAC plugin) {
        super(plugin, "AutoClicker", "G", CheckType.COMBAT);
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

        double skewness = MathUtil.skewness(intervals);
        double mean = MathUtil.mean(intervals);

        // Near-zero skewness with high CPS = auto-clicker
        if (skewness > -0.3 && skewness < 0.2 && mean < 80) {
            handleViolation(data, player, 3.0, 12.0, 1.0);
        }
    }
}
