package com.yourdomain.guardianac.checks.combat.hitbox;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * HitBox (F) — Hitbox width expansion detection.
 * Measures horizontal offset between look direction and target bounding box.
 * Large offsets indicate expanded hitbox width.
 */
public class HitBoxF extends Check {

    private static final double MAX_HORIZONTAL_OFFSET = 0.7;

    public HitBoxF(GuardianAC plugin) {
        super(plugin, "HitBox", "F", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Vector eye = player.getEyeLocation().toVector();
        Vector dir = player.getEyeLocation().getDirection().normalize();
        Vector targetCenter = target.getBoundingBox().getCenter();

        Vector eyeToTarget = targetCenter.clone().subtract(eye);
        double projection = eyeToTarget.dot(dir);
        Vector closestOnRay = eye.clone().add(dir.clone().multiply(projection));

        double dx = closestOnRay.getX() - targetCenter.getX();
        double dz = closestOnRay.getZ() - targetCenter.getZ();
        double horizontalOffset = Math.sqrt(dx * dx + dz * dz);

        double maxOffset = MAX_HORIZONTAL_OFFSET + PingUtil.getReachBuffer(player) * 0.3;
        double targetHalfWidth = target.getBoundingBox().getWidthX() / 2.0;

        if (horizontalOffset > targetHalfWidth + maxOffset) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
        }
    }
}
