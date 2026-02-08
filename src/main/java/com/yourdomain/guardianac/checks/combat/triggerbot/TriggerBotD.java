package com.yourdomain.guardianac.checks.combat.triggerbot;

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
 * TriggerBot (D) — Consistent reaction time analysis.
 * Detects unnaturally consistent timing between target entering
 * crosshair and attack firing, indicating automated reactions.
 */
public class TriggerBotD extends Check {

    private static final int MIN_SAMPLES = 8;

    public TriggerBotD(GuardianAC plugin) {
        super(plugin, "TriggerBot", "D", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        List<Long> intervals = data.getAttackIntervals();
        if (intervals.size() < MIN_SAMPLES) return;

        double stdDev = MathUtil.standardDeviation(intervals);
        double mean = MathUtil.mean(intervals);

        if (mean < 1) return;

        double cv = stdDev / mean;

        if (cv < 0.05 && mean < 200 && mean > 30) {
            handleViolation(data, player, 5.0, 12.0, 1.0);
        }
    }
}
