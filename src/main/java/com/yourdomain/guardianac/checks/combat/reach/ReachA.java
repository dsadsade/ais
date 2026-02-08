package com.yourdomain.guardianac.checks.combat.reach;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Reach (A) — Single hit distance check.
 * Measures the distance from the player's eye to the target entity's hitbox.
 * Vanilla reach is 3.0 blocks; buffer scales dynamically with player ping.
 */
public class ReachA extends Check {

    private static final double VANILLA_REACH = 3.0;
    private static final double BASE_BUFFER = 0.1;

    public ReachA(GuardianAC plugin) {
        super(plugin, "Reach", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double distance = getDistanceToEntity(player.getEyeLocation(), target);
        double allowedReach = VANILLA_REACH + BASE_BUFFER + PingUtil.getReachBuffer(player);

        data.addReachDistance(distance);

        if (distance > allowedReach) {
            double excess = distance - allowedReach;
            // Weight scales with how far over the limit
            double weight = Math.min(4.0, 1.0 + excess * 3.0);
            handleViolation(data, player, weight, 6.0, 0.5);
        }
    }

    private double getDistanceToEntity(Location eyeLoc, LivingEntity target) {
        double targetX = target.getLocation().getX();
        double targetY = target.getLocation().getY() + (target.getHeight() / 2.0);
        double targetZ = target.getLocation().getZ();

        double halfWidth = target.getWidth() / 2.0;
        double halfHeight = target.getHeight() / 2.0;

        double closestX = clamp(eyeLoc.getX(), targetX - halfWidth, targetX + halfWidth);
        double closestY = clamp(eyeLoc.getY(), targetY - halfHeight, targetY + halfHeight);
        double closestZ = clamp(eyeLoc.getZ(), targetZ - halfWidth, targetZ + halfWidth);

        double dx = eyeLoc.getX() - closestX;
        double dy = eyeLoc.getY() - closestY;
        double dz = eyeLoc.getZ() - closestZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
