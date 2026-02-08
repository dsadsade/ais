package com.yourdomain.guardianac.checks.combat.hitbox;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * HitBox (D) — Extended Y-axis hitbox detection.
 * Detects hits where the attacker's eye height is far outside
 * the target's bounding box vertically.
 */
public class HitBoxD extends Check {

    private static final double MAX_Y_EXTENSION = 0.5;

    public HitBoxD(GuardianAC plugin) {
        super(plugin, "HitBox", "D", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double eyeY = player.getEyeLocation().getY();
        double targetMaxY = target.getBoundingBox().getMaxY();
        double targetMinY = target.getBoundingBox().getMinY();

        if (eyeY > targetMaxY) {
            double horizontalDist = player.getLocation().distance(target.getLocation());
            if (horizontalDist > 0.1) {
                double requiredPitch = Math.toDegrees(Math.atan2(eyeY - targetMaxY, horizontalDist));
                double actualPitch = player.getLocation().getPitch();
                if (actualPitch < requiredPitch - MAX_Y_EXTENSION * 10) {
                    handleViolation(data, player, 4.0, 10.0, 1.0);
                }
            }
        } else if (eyeY < targetMinY - MAX_Y_EXTENSION) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
        }
    }
}
