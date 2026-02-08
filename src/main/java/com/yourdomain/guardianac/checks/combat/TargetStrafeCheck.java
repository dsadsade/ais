package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

/**
 * Detects TargetStrafe — clients that perfectly circle-strafe around targets.
 * Legitimate players have erratic and inconsistent angular changes relative to
 * their target. TargetStrafe mods maintain a nearly constant angular velocity
 * forming perfect or near-perfect circles around the target.
 */
public class TargetStrafeCheck extends Check {

    private static final double MAX_ANGLE_DEVIATION = 3.0; // degrees — too consistent = bot
    private static final int MIN_SAMPLES = 8;

    public TargetStrafeCheck(GuardianAC plugin) {
        super(plugin, "TargetStrafe", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location playerLoc = player.getLocation();
        Location targetLoc = target.getLocation();

        // Calculate angle from target to player
        double dx = playerLoc.getX() - targetLoc.getX();
        double dz = playerLoc.getZ() - targetLoc.getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx));

        double lastAngle = data.getLastTargetStrafeAngle();
        if (lastAngle != 0) {
            double deltaAngle = MathUtil.wrapAngle((float) (angle - lastAngle));
            data.addTargetStrafeAngle(Math.abs(deltaAngle));
        }
        data.setLastTargetStrafeAngle(angle);

        List<Double> angles = data.getTargetStrafeAngles();
        if (angles.size() >= MIN_SAMPLES) {
            // Check standard deviation — low deviation = robotic strafing
            double[] arr = angles.stream().mapToDouble(Double::doubleValue).toArray();
            double stdDev = MathUtil.standardDeviation(arr);
            double avg = 0;
            for (double v : arr) avg += v;
            avg /= arr.length;

            // Very consistent angular change with a reasonable average = target strafe
            if (stdDev < MAX_ANGLE_DEVIATION && avg > 5.0 && avg < 60.0) {
                handleViolation(data, player, 4.0, 12.0, 1.0);
            }
        }
    }
}
