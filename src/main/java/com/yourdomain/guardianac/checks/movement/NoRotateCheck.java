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
 * Detects NoRotate / AntiAim — the player moves in a direction that doesn't
 * match their head rotation. Legitimate players face the direction they move in.
 * AntiAim mods spoof the head rotation to confuse other players' aim while
 * actually moving in a completely different direction.
 */
public class NoRotateCheck extends Check {

    private static final double MAX_DIRECTION_MISMATCH = 90.0; // degrees
    private static final double MIN_MOVE_SPEED = 0.15; // ignore slow movements
    private static final int MAX_FLAGS = 10;

    public NoRotateCheck(GuardianAC plugin) {
        super(plugin, "NoRotate", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (speed < MIN_MOVE_SPEED) return;
        if (!player.isSprinting()) return; // only check while sprinting

        // Calculate the actual movement direction
        double moveAngle = Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        if (moveAngle < 0) moveAngle += 360;

        // Player's reported yaw
        float yaw = event.getTo().getYaw() % 360;
        if (yaw < 0) yaw += 360;

        // Difference between look direction and movement direction
        double diff = Math.abs(moveAngle - yaw);
        if (diff > 180) diff = 360 - diff;

        data.setLastMovementDirection(moveAngle);

        // While sprinting, direction and look should be roughly aligned
        if (diff > MAX_DIRECTION_MISMATCH) {
            data.setNoRotateFlags(data.getNoRotateFlags() + 1);

            if (data.getNoRotateFlags() >= MAX_FLAGS) {
                handleViolation(data, player, 3.0, 12.0, 1.0);
                data.setNoRotateFlags(0);
            }
        } else {
            data.setNoRotateFlags(Math.max(0, data.getNoRotateFlags() - 1));
        }
    }
}
