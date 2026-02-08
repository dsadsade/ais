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
import org.bukkit.util.Vector;

/**
 * Reach (C) — Moving target compensation.
 * Factors in target velocity when calculating reach.
 * Moving targets may appear closer or farther due to interpolation lag.
 * This check compensates for movement and is more accurate for strafing fights.
 */
public class ReachC extends Check {

    private static final double VANILLA_REACH = 3.0;

    public ReachC(GuardianAC plugin) {
        super(plugin, "Reach", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location eyeLoc = player.getEyeLocation();
        Location targetLoc = target.getLocation();
        Location lastTargetLoc = data.getLastTargetLocation();

        // Calculate base distance
        double distance = getDistanceToEntity(eyeLoc, target);

        // Calculate movement-compensated distance if we have a previous location
        double compensatedDistance = distance;
        if (lastTargetLoc != null && lastTargetLoc.getWorld() == targetLoc.getWorld()) {
            // Estimate where the target could have been when the hit registered
            Vector targetVelocity = targetLoc.toVector().subtract(lastTargetLoc.toVector());
            double extrapolationFactor = PingUtil.getPing(player) / 50.0; // ping in ticks
            Location extrapolatedLoc = targetLoc.clone().add(
                    targetVelocity.multiply(Math.min(extrapolationFactor, 4.0)));

            double extrapolatedDist = eyeLoc.distance(
                    extrapolatedLoc.clone().add(0, target.getHeight() / 2.0, 0));

            // Use the shorter of the two distances (benefit of the doubt)
            compensatedDistance = Math.min(distance, extrapolatedDist);
        }

        data.setLastTargetLocation(targetLoc.clone());

        // Dynamic buffer: base + ping + movement compensation
        double pingBuffer = PingUtil.getReachBuffer(player);
        double movementBuffer = 0.15; // extra for moving targets
        double allowedReach = VANILLA_REACH + 0.1 + pingBuffer + movementBuffer;

        if (compensatedDistance > allowedReach) {
            double excess = compensatedDistance - allowedReach;
            handleViolation(data, player, Math.min(3.5, 1.0 + excess * 3.0), 6.0, 0.5);
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
