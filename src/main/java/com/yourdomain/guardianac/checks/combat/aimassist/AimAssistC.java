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

import java.util.List;

/**
 * AimAssist (C) — Constant Rotation Speed Detection
 * Detects aim assist that moves at a constant angular velocity toward targets.
 * Legitimate mouse movements have acceleration/deceleration phases.
 * Aim assists that interpolate linearly produce suspiciously uniform rotation speeds.
 */
public class AimAssistC extends Check {

    private static final int MIN_SAMPLES = 12;
    private static final double MAX_KURTOSIS = -0.8; // platykurtic = too uniform

    public AimAssistC(GuardianAC plugin) {
        super(plugin, "AimAssist", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        float yawDelta = Math.abs(yaw - data.getLastYaw());
        if (yawDelta > 180) yawDelta = 360 - yawDelta;

        data.addSmoothYawDelta(yawDelta);

        List<Float> yawDeltas = data.getSmoothYawDeltas();
        if (yawDeltas.size() >= MIN_SAMPLES) {
            double kurtosis = MathUtil.kurtosis(yawDeltas);

            // Highly platykurtic (flat) distribution = constant rotation speed
            if (kurtosis < MAX_KURTOSIS) {
                double stdDev = MathUtil.standardDeviation(yawDeltas);
                // Only flag if there's actual rotation happening (not just standing still)
                if (stdDev > 0.5 && stdDev < 5.0) {
                    handleViolation(data, player, 3.0, 10.0, 1.0);
                }
            }
        }
    }
}
