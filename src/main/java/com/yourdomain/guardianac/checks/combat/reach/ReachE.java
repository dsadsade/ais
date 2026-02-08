package com.yourdomain.guardianac.checks.combat.reach;

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

/**
 * Reach (E) — Eye-to-hitbox center distance.
 * Calculates precise distance from eye position to the center of
 * the target's bounding box for accurate reach measurement.
 */
public class ReachE extends Check {

    private static final double MAX_REACH = 3.1;

    public ReachE(GuardianAC plugin) {
        super(plugin, "Reach", "E", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location eye = player.getEyeLocation();
        double centerX = (target.getBoundingBox().getMinX() + target.getBoundingBox().getMaxX()) / 2.0;
        double centerY = (target.getBoundingBox().getMinY() + target.getBoundingBox().getMaxY()) / 2.0;
        double centerZ = (target.getBoundingBox().getMinZ() + target.getBoundingBox().getMaxZ()) / 2.0;

        double dx = eye.getX() - centerX;
        double dy = eye.getY() - centerY;
        double dz = eye.getZ() - centerZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double maxAllowed = MAX_REACH + PingUtil.getReachBuffer(player);
        if (distance > maxAllowed) {
            handleViolation(data, player, (distance - maxAllowed) * 5.0, 10.0, 0.5);
        }
    }
}
