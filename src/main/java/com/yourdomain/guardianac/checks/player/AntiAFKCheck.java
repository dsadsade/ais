package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

/**
 * Detects AntiAFK bots — automated movement patterns to prevent AFK kicks.
 * AFK bots typically move in repetitive geometric patterns (circles, back-and-forth)
 * with robotic precision. Detected by analyzing movement angle changes for
 * suspiciously consistent repeating patterns over long periods.
 */
public class AntiAFKCheck extends Check {

    private static final int MIN_ANGLE_SAMPLES = 20;
    private static final double MAX_PATTERN_DEVIATION = 5.0; // degrees
    private static final long AFK_DETECTION_INTERVAL_MS = 5000; // check every 5 seconds

    public AntiAFKCheck(GuardianAC plugin) {
        super(plugin, "AntiAFK", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (speed < 0.05) return; // standing still

        double moveAngle = Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        data.addAfkMovementAngle(moveAngle);
        data.setLastActualInputTime(System.currentTimeMillis());

        List<Double> angles = data.getAfkMovementAngles();
        if (angles.size() < MIN_ANGLE_SAMPLES) return;

        // Check for repeating pattern: calculate angle-to-angle deltas
        double[] deltas = new double[angles.size() - 1];
        for (int i = 1; i < angles.size(); i++) {
            deltas[i - 1] = angles.get(i) - angles.get(i - 1);
        }

        double stdDev = MathUtil.standardDeviation(deltas);

        // Very consistent angular pattern = bot
        if (stdDev < MAX_PATTERN_DEVIATION) {
            data.setAfkPatternCount(data.getAfkPatternCount() + 1);

            if (data.getAfkPatternCount() >= 3) {
                handleViolation(data, player, 3.0, 15.0, 1.0);
                data.setAfkPatternCount(0);
            }
        } else {
            data.setAfkPatternCount(Math.max(0, data.getAfkPatternCount() - 1));
        }
    }
}
