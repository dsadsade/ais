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
 * KillAura (J) — Acceleration / mouse smoothness analysis.
 * Analyzes the delta-delta (acceleration) of yaw/pitch changes on attack ticks.
 * Aimbot rotations have unnatural acceleration profiles: either perfectly linear
 * (constant speed) or showing sharp discontinuities.
 */
public class KillAuraJ extends Check {

    private static final int MIN_SAMPLES = 10;

    public KillAuraJ(GuardianAC plugin) {
        super(plugin, "KillAura", "J", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        float deltaYaw = Math.abs(wrapAngle(yaw - data.getLastYaw()));
        float deltaPitch = Math.abs(pitch - data.getLastPitch());

        data.addAttackYawDelta(deltaYaw);
        data.addAttackPitchDelta(deltaPitch);

        List<Float> yawDeltas = data.getAttackYawDeltas();
        List<Float> pitchDeltas = data.getAttackPitchDeltas();

        if (yawDeltas.size() < MIN_SAMPLES) return;

        // Check for perfectly linear rotation (constant acceleration = aimbot smoothing)
        double yawStdDev = MathUtil.standardDeviation(yawDeltas);
        double pitchStdDev = MathUtil.standardDeviation(pitchDeltas);
        double yawMean = yawDeltas.stream().mapToDouble(Float::doubleValue).average().orElse(0);
        double pitchMean = pitchDeltas.stream().mapToDouble(Float::doubleValue).average().orElse(0);

        // Near-zero std dev on attack rotations = robot-like precision
        if (yawMean > 1.0 && pitchMean > 0.5) {
            double yawCv = yawStdDev / yawMean;
            double pitchCv = pitchStdDev / pitchMean;

            if (yawCv < 0.05 && pitchCv < 0.05 && yawDeltas.size() >= 15) {
                handleViolation(data, player, 2.5, 8.0, 0.5);
            } else if (yawCv < 0.1 && pitchCv < 0.1 && yawDeltas.size() >= 20) {
                handleViolation(data, player, 1.0, 8.0, 0.5);
            }
        }

        // Check for outlier/discontinuity — sudden large delta among small ones
        int outliers = MathUtil.countOutliers(yawDeltas);
        if (outliers == 0 && yawDeltas.size() >= 15 && yawStdDev < 1.0) {
            // Perfect consistency with zero outliers = too clean
            handleViolation(data, player, 1.5, 8.0, 0.5);
        }
    }

    private float wrapAngle(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }
}
