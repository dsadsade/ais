package com.yourdomain.guardianac.checks.combat.aimbot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Aimbot B — detects predictive aim patterns.
 * Advanced aimbots predict where a target will be and pre-aim.
 * This creates an unnatural pattern where the player's aim arrives at the
 * target's future position before the target does, resulting in
 * suspiciously consistent angular prediction deltas.
 */
public class AimbotB extends Check {

    private static final double MAX_PREDICTION_CONSISTENCY = 2.5; // std dev threshold
    private static final int MIN_SAMPLES = 6;

    public AimbotB(GuardianAC plugin) {
        super(plugin, "Aimbot", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location playerEye = player.getEyeLocation();
        Vector lookDir = playerEye.getDirection().normalize();

        // Get target velocity to check if aim is predictive
        Vector targetVel = target.getVelocity();
        if (targetVel.lengthSquared() < 0.01) return; // target not moving — skip

        // Predicted position if target continues moving
        Location predictedLoc = target.getLocation().add(targetVel.multiply(3)); // 3 ticks ahead
        Vector toPredicted = predictedLoc.toVector().subtract(playerEye.toVector()).normalize();

        // How close is the player's aim to the predicted position?
        double dot = lookDir.dot(toPredicted);
        double predictionAngle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));

        data.addAimPredictionDelta(predictionAngle);

        List<Double> deltas = data.getAimPredictionDeltas();
        if (deltas.size() >= MIN_SAMPLES) {
            double[] arr = deltas.stream().mapToDouble(Double::doubleValue).toArray();
            double stdDev = MathUtil.standardDeviation(arr);
            double avg = 0;
            for (double v : arr) avg += v;
            avg /= arr.length;

            // Very consistent aim toward predicted positions = aimbot
            if (stdDev < MAX_PREDICTION_CONSISTENCY && avg < 10.0) {
                handleViolation(data, player, 4.0, 12.0, 1.0);
            }
        }
    }
}
