package com.yourdomain.guardianac.checks.combat.aimassist;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

/**
 * AimAssist (D) — Horizontal aim lock detection (yaw-only snaps).
 * Detects aim assist that only adjusts yaw while keeping pitch constant,
 * common in horizontal-only aim assists.
 */
public class AimAssistD extends Check {

    private static final int MIN_SAMPLES = 8;
    private static final float PITCH_THRESHOLD = 0.5f;

    public AimAssistD(GuardianAC plugin) {
        super(plugin, "AimAssist", "D", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yawDelta = Math.abs(player.getLocation().getYaw() - data.getLastYaw());
        if (yawDelta > 180) yawDelta = 360 - yawDelta;
        float pitchDelta = Math.abs(player.getLocation().getPitch() - data.getLastPitch());

        data.addSmoothYawDelta(yawDelta);
        data.addSmoothPitchDelta(pitchDelta);

        List<Float> yawDeltas = data.getSmoothYawDeltas();
        List<Float> pitchDeltas = data.getSmoothPitchDeltas();
        if (yawDeltas.size() < MIN_SAMPLES) return;

        int yawOnlyCount = 0;
        int size = Math.min(yawDeltas.size(), pitchDeltas.size());
        for (int i = size - MIN_SAMPLES; i < size; i++) {
            if (yawDeltas.get(i) > 3.0f && pitchDeltas.get(i) < PITCH_THRESHOLD) {
                yawOnlyCount++;
            }
        }

        if (yawOnlyCount >= MIN_SAMPLES - 2) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
        }
    }
}
