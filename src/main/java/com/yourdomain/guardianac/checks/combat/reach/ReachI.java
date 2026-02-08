package com.yourdomain.guardianac.checks.combat.reach;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * Reach (I) — Reach through entities.
 * Detects hits that pass through another entity between attacker and target.
 * In vanilla, attacks interact with the nearest entity in the look direction.
 */
public class ReachI extends Check {

    public ReachI(GuardianAC plugin) {
        super(plugin, "Reach", "I", CheckType.COMBAT);
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
        double targetDist = player.getEyeLocation().distance(target.getLocation());

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(), direction, targetDist + 0.5,
                e -> e != player && e instanceof LivingEntity
        );

        if (result != null && result.getHitEntity() != null) {
            Entity hitEntity = result.getHitEntity();
            if (hitEntity.getEntityId() != target.getEntityId()) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        }
    }
}
