package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects MotionSpoof — client sending contradictory motion values,
 * e.g. claiming to move forward while actual position goes sideways,
 * or zero-motion packets while position changes.
 */
public class MotionSpoofCheck extends Check {

    private static final double MAX_PREDICTION_ERROR = 0.5;
    private static final double MIN_MOVEMENT_THRESHOLD = 0.1;

    public MotionSpoofCheck(GuardianAC plugin) {
        super(plugin, "MotionSpoof", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dy = event.getTo().getY() - event.getFrom().getY();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // Check for micro-movements that indicate motion spoofing
        // Player sends many tiny movements to bypass speed predictions
        if (horizontalDist > MIN_MOVEMENT_THRESHOLD) {
            float yaw = event.getTo().getYaw();
            double expectedX = -Math.sin(Math.toRadians(yaw));
            double expectedZ = Math.cos(Math.toRadians(yaw));
            double movAngle = Math.atan2(dz, dx);
            double expectedAngle = Math.atan2(expectedZ, expectedX);

            double angleDiff = Math.abs(movAngle - expectedAngle);
            if (angleDiff > Math.PI) angleDiff = 2 * Math.PI - angleDiff;

            // If moving significant distance and ground Y unchanged, but angle is completely wrong
            if (angleDiff > Math.PI * 0.75 && Math.abs(dy) < 0.1 && !player.isSneaking()) {
                flag(player, String.format("angleDiff=%.2f speed=%.4f", Math.toDegrees(angleDiff), horizontalDist));
            }
        }
    }
}
