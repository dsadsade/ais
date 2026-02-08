package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

/**
 * KillAura (O) — Multi-tick aim interpolation analysis.
 * Detects linear (non-natural) aim interpolation. Human aim has acceleration
 * phases; aimbots use linear interpolation producing uniform delta sequences.
 */
public class KillAuraO extends Check {

    private static final int MIN_SAMPLES = 8;

    public KillAuraO(GuardianAC plugin) {
        super(plugin, "KillAura", "O", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        List<Float> yawDeltas = data.getYawDeltaHistory();
        if (yawDeltas.size() < MIN_SAMPLES) return;

        List<Float> recent = yawDeltas.subList(yawDeltas.size() - MIN_SAMPLES, yawDeltas.size());
        double stdDev = MathUtil.standardDeviation(recent);
        double mean = MathUtil.mean(recent);

        // Very low std dev with significant rotation = linear interpolation
        if (mean > 2.0 && stdDev < 0.3 && stdDev > 0.0) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
        }
    }
}
