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
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * HitBox (C) — Phantom hit detection (damage without ray trace intersection).
 * Checks if the player's look direction doesn't intersect the target's
 * bounding box at all, indicating an expanded/phantom hitbox.
 */
public class HitBoxC extends Check {

    private static final double RAY_LENGTH = 4.5;

    public HitBoxC(GuardianAC plugin) {
        super(plugin, "HitBox", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Vector direction = player.getEyeLocation().getDirection();
        BoundingBox box = target.getBoundingBox().expand(PingUtil.getReachBuffer(player) + 0.1);

        RayTraceResult result = box.rayTrace(
                player.getEyeLocation().toVector(), direction, RAY_LENGTH);

        if (result == null) {
            Vector right = direction.clone().rotateAroundY(Math.toRadians(5));
            Vector left = direction.clone().rotateAroundY(Math.toRadians(-5));
            RayTraceResult r1 = box.rayTrace(player.getEyeLocation().toVector(), right, RAY_LENGTH);
            RayTraceResult r2 = box.rayTrace(player.getEyeLocation().toVector(), left, RAY_LENGTH);

            if (r1 == null && r2 == null) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        }
    }
}
