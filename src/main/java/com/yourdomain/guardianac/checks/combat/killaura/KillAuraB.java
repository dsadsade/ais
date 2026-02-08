package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

/**
 * KillAura (B) — Hit consistency / timing analysis.
 * Detects suspiciously consistent timing between attacks.
 * Legitimate players have natural variance; aura clients fire at fixed intervals.
 */
public class KillAuraB extends Check {

    private static final int MIN_SAMPLES = 8;

    public KillAuraB(GuardianAC plugin) {
        super(plugin, "KillAura", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        if (data.getLastAttackTime() > 0) {
            double interval = now - data.getLastAttackTime();
            data.addAttackInterval(interval);
        }
        data.addAttackTimestamp(now);
        data.setLastAttackTime(now);

        List<Double> intervals = data.getAttackIntervals();
        if (intervals.size() < MIN_SAMPLES) return;

        double stdDev = MathUtil.standardDeviation(intervals);
        double mean = intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        // Coefficient of variation: stdDev/mean — very low = suspiciously consistent
        if (mean > 0) {
            double cv = stdDev / mean;
            // Legitimate players typically have CV > 0.15
            // Aura clients have CV < 0.05
            if (cv < 0.04 && intervals.size() >= 12) {
                handleViolation(data, player, 3.0, 7.0, 0.5);
            } else if (cv < 0.08 && intervals.size() >= 15) {
                handleViolation(data, player, 1.5, 7.0, 0.5);
            }
        }
    }
}
