package com.yourdomain.guardianac.checks.combat.reach;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

/**
 * Reach (B) — Rolling average analysis.
 * Instead of checking single hits, this analyzes the average reach distance
 * over a rolling window. Catches subtle reach extensions that stay under
 * the single-hit threshold.
 */
public class ReachB extends Check {

    private static final int SAMPLE_SIZE = 12;
    private static final double MAX_AVERAGE_REACH = 3.15;

    public ReachB(GuardianAC plugin) {
        super(plugin, "Reach", "B", CheckType.COMBAT);
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
        data.addReachDistance(distance);

        List<Double> distances = data.getRecentReachDistances();
        if (distances.size() < SAMPLE_SIZE) return;

        // Take the last N samples
        List<Double> window = distances.subList(
                Math.max(0, distances.size() - SAMPLE_SIZE), distances.size());

        double avg = window.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double stdDev = MathUtil.standardDeviation(window);

        // Consistently high average with low variance = reach hack
        if (avg > MAX_AVERAGE_REACH && stdDev < 0.3) {
            double excess = avg - MAX_AVERAGE_REACH;
            handleViolation(data, player, 1.5 + excess * 4.0, 7.0, 0.5);
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
