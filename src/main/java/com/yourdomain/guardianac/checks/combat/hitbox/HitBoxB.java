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
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * HitBox (B) — Ray Trace Miss Detection
 * Performs a ray trace from the player's eye along their look direction.
 * If the ray doesn't intersect the target's bounding box (with a small tolerance),
 * the hit should not have connected, indicating an expanded hitbox.
 */
public class HitBoxB extends Check {

    private static final double RAY_LENGTH = 6.0;
    private static final double BOX_EXPANSION = 0.15; // small tolerance for lag

    public HitBoxB(GuardianAC plugin) {
        super(plugin, "HitBox", "B", CheckType.COMBAT);
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
        Vector direction = eye.getDirection();

        // Expand bounding box slightly based on ping
        double expansion = BOX_EXPANSION + PingUtil.getReachBuffer(player);
        BoundingBox box = target.getBoundingBox().expand(expansion);

        RayTraceResult result = box.rayTrace(eye.toVector(), direction, RAY_LENGTH);

        if (result == null) {
            // Ray didn't hit the expanded bounding box but damage event fired
            handleViolation(data, player, 4.0, 10.0, 1.0);
        }
    }
}
