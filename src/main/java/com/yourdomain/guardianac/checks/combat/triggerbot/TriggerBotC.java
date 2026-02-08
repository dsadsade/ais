package com.yourdomain.guardianac.checks.combat.triggerbot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * TriggerBot (C) — Instant attack on FOV enter.
 * Flags when a player attacks within 1-2 ticks of the target
 * entering their field of view for multiple consecutive hits.
 */
public class TriggerBotC extends Check {

    private static final double FOV_ANGLE = 10.0;
    private static final int CONSECUTIVE_THRESHOLD = 4;

    public TriggerBotC(GuardianAC plugin) {
        super(plugin, "TriggerBot", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Vector prevLook = getDirectionFromYawPitch(data.getLastYaw(), data.getLastPitch());
        Vector toTarget = target.getEyeLocation().toVector()
                .subtract(player.getEyeLocation().toVector()).normalize();

        double prevAngle = Math.toDegrees(prevLook.angle(toTarget));
        double currAngle = Math.toDegrees(player.getEyeLocation().getDirection().angle(toTarget));

        if (prevAngle > FOV_ANGLE && currAngle < FOV_ANGLE) {
            data.setConsecutiveImmediateHits(data.getConsecutiveImmediateHits() + 1);
        } else {
            data.setConsecutiveImmediateHits(Math.max(0, data.getConsecutiveImmediateHits() - 1));
        }

        if (data.getConsecutiveImmediateHits() >= CONSECUTIVE_THRESHOLD) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
        }
    }

    private Vector getDirectionFromYawPitch(float yaw, float pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vector(x, y, z).normalize();
    }
}
