package com.yourdomain.guardianac.checks.combat.nohitdelay;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

/**
 * NoHitDelay (C) — Hit timing under vanilla minimum.
 * Analyzes attack interval statistics to detect a pattern of hits
 * consistently below the vanilla minimum attack interval.
 */
public class NoHitDelayC extends Check {

    private static final long VANILLA_MIN_INTERVAL_MS = 500;
    private static final int MIN_SAMPLES = 6;

    public NoHitDelayC(GuardianAC plugin) {
        super(plugin, "NoHitDelay", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        List<Double> intervals = data.getAttackIntervals();
        if (intervals.size() < MIN_SAMPLES) return;

        double mean = MathUtil.mean(intervals);
        long countUnderMin = intervals.stream()
                .filter(i -> i < VANILLA_MIN_INTERVAL_MS)
                .count();

        double ratio = (double) countUnderMin / intervals.size();

        if (ratio > 0.7 && mean < VANILLA_MIN_INTERVAL_MS * 0.8) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
        }
    }
}
