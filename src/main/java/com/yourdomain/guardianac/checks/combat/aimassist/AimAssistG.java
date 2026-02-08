package com.yourdomain.guardianac.checks.combat.aimassist;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * AimAssist (G) — Rotation acceleration patterns.
 * Analyzes the second derivative of rotation (acceleration). Aim assists
 * produce abrupt acceleration changes with unnatural jerk profiles.
 */
public class AimAssistG extends Check {

    private static final int MIN_SAMPLES = 10;

    public AimAssistG(GuardianAC plugin) {
        super(plugin, "AimAssist", "G", CheckType.COMBAT);
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

        List<Float> accelerations = new ArrayList<>();
        for (int i = yawDeltas.size() - MIN_SAMPLES + 1; i < yawDeltas.size(); i++) {
            accelerations.add(Math.abs(yawDeltas.get(i) - yawDeltas.get(i - 1)));
        }

        double accelStdDev = MathUtil.standardDeviation(accelerations);
        double accelMean = MathUtil.mean(accelerations);

        if (accelStdDev < 0.5 && accelMean > 0.1 && accelMean < 3.0) {
            handleViolation(data, player, 3.0, 12.0, 1.0);
        }
    }
}
