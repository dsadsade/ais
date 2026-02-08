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
 * AimAssist (E) — Vertical aim assist (pitch smoothing).
 * Detects unnaturally smooth pitch adjustments toward targets with
 * suspiciously low variance compared to natural mouse movement.
 */
public class AimAssistE extends Check {

    private static final int MIN_SAMPLES = 10;
    private static final double MAX_PITCH_VARIANCE = 0.5;

    public AimAssistE(GuardianAC plugin) {
        super(plugin, "AimAssist", "E", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float pitchDelta = Math.abs(player.getLocation().getPitch() - data.getLastPitch());
        data.addSmoothPitchDelta(pitchDelta);

        List<Float> pitchDeltas = data.getSmoothPitchDeltas();
        if (pitchDeltas.size() < MIN_SAMPLES) return;

        List<Float> recent = pitchDeltas.subList(pitchDeltas.size() - MIN_SAMPLES, pitchDeltas.size());
        double variance = MathUtil.variance(recent);
        double mean = MathUtil.mean(recent);

        if (variance < MAX_PITCH_VARIANCE && variance > 0.0 && mean > 1.0) {
            handleViolation(data, player, 3.0, 12.0, 1.0);
        }
    }
}
