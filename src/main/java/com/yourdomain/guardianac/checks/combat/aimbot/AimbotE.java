package com.yourdomain.guardianac.checks.combat.aimbot;

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
 * Aimbot (E) — Aim smoothing speed (constant rotation speed).
 * Detects aimbots that use fixed-speed smoothing producing
 * uniform angular velocity distribution.
 */
public class AimbotE extends Check {

    private static final int MIN_SAMPLES = 10;

    public AimbotE(GuardianAC plugin) {
        super(plugin, "Aimbot", "E", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        float yawDelta = Math.abs(yaw - data.getLastYaw());
        if (yawDelta > 180) yawDelta = 360 - yawDelta;

        float totalDelta = (float) Math.sqrt(yawDelta * yawDelta +
                Math.pow(pitch - data.getLastPitch(), 2));

        data.addAimLockSample(totalDelta);

        List<Float> samples = data.getAimLockSamples();
        if (samples.size() < MIN_SAMPLES) return;

        double stdDev = MathUtil.standardDeviation(samples);
        double mean = MathUtil.mean(samples);

        if (stdDev < 1.0 && mean > 3.0 && mean < 20.0) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
        }
    }
}
