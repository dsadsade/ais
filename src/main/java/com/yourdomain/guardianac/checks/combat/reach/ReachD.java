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

import java.util.List;

/**
 * Reach D — detects vertical reach exploits.
 * Checks the Y-axis distance between attacker and target separately.
 * Some reach mods only extend horizontal reach but vertical reach exploits
 * allow hitting entities far above or below the player.
 */
public class ReachD extends Check {

    private static final double MAX_VERTICAL_REACH = 4.0; // blocks
    private static final int MIN_SAMPLES = 5;
    private static final double SUSPICIOUS_AVG_VERTICAL = 3.2;

    public ReachD(GuardianAC plugin) {
        super(plugin, "Reach", "D", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location playerEye = player.getEyeLocation();
        Location targetLoc = target.getLocation();

        // Vertical distance component
        double verticalDist = Math.abs(playerEye.getY() - targetLoc.getY());
        double maxReach = MAX_VERTICAL_REACH + PingUtil.getReachBuffer(player) * 0.5;

        // Instant flag for extreme vertical reach
        if (verticalDist > maxReach) {
            handleViolation(data, player, (verticalDist - maxReach) * 5.0, 10.0, 0.5);
            return;
        }

        data.addVerticalReachDistance(verticalDist);

        List<Double> distances = data.getVerticalReachDistances();
        if (distances.size() >= MIN_SAMPLES) {
            double avg = distances.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            if (avg > SUSPICIOUS_AVG_VERTICAL) {
                handleViolation(data, player, (avg - SUSPICIOUS_AVG_VERTICAL) * 4.0, 12.0, 1.0);
            }
        }
    }
}
