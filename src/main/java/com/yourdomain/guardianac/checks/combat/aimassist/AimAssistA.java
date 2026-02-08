package com.yourdomain.guardianac.checks.combat.aimassist;

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

import java.util.List;

/**
 * AimAssist (A) — Smooth Aim Detection
 * Detects aim assist that smooths rotation to target entities.
 * Legitimate players have variable rotation deltas with natural jitter.
 * Aim assists produce unnaturally smooth, low-deviation rotation patterns.
 */
public class AimAssistA extends Check {

    private static final double MIN_STD_DEV_YAW = 0.15;
    private static final int MIN_SAMPLES = 10;

    public AimAssistA(GuardianAC plugin) {
        super(plugin, "AimAssist", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location loc = player.getLocation();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();

        // Calculate rotation delta from last attack
        float yawDelta = Math.abs(yaw - data.getLastYaw());
        float pitchDelta = Math.abs(pitch - data.getLastPitch());

        // Wrap yaw delta
        if (yawDelta > 180) yawDelta = 360 - yawDelta;

        data.addSmoothYawDelta(yawDelta);
        data.addSmoothPitchDelta(pitchDelta);

        List<Float> yawDeltas = data.getSmoothYawDeltas();
        if (yawDeltas.size() >= MIN_SAMPLES) {
            double stdDev = MathUtil.standardDeviation(yawDeltas);

            // Very low standard deviation means unnaturally smooth aiming
            if (stdDev < MIN_STD_DEV_YAW && stdDev > 0.001) {
                handleViolation(data, player, 4.0, 12.0, 1.5);
            }
        }
    }
}
