package com.yourdomain.guardianac.checks.combat.hitbox;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * HitBox (A) — Distance to Hitbox Edge Detection
 * Detects expanded hitbox hacks by measuring the angle between the player's
 * look direction and the target entity. With an expanded hitbox, players can hit
 * entities while looking significantly away from them.
 */
public class HitBoxA extends Check {

    private static final double MAX_ANGLE_DEGREES = 55.0; // vanilla is roughly 45-50

    public HitBoxA(GuardianAC plugin) {
        super(plugin, "HitBox", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location eyeLocation = player.getEyeLocation();
        Vector lookDirection = eyeLocation.getDirection().normalize();

        // Vector from player's eye to target center
        Location targetCenter = target.getLocation().add(0, target.getHeight() / 2.0, 0);
        Vector toTarget = targetCenter.toVector().subtract(eyeLocation.toVector()).normalize();

        double angle = Math.toDegrees(lookDirection.angle(toTarget));

        double maxAngle = MAX_ANGLE_DEGREES * PingUtil.getRotationMultiplier(player);

        if (angle > maxAngle) {
            double excess = angle - maxAngle;
            handleViolation(data, player, excess * 0.5, 12.0, 1.5);
        }
    }
}
