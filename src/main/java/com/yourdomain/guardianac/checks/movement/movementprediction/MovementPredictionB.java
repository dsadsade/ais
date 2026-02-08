package com.yourdomain.guardianac.checks.movement.movementprediction;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * MovementPrediction (B) — Movement path anomaly detection.
 * Flags sudden direction changes that defy momentum physics.
 */
public class MovementPredictionB extends Check {

    private static final double ANGLE_CHANGE_THRESHOLD = 120.0;

    public MovementPredictionB(GuardianAC plugin) {
        super(plugin, "MovementPrediction", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle() || player.isGliding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double prevDX = data.getLastDeltaX();
        double prevDZ = data.getLastDeltaZ();

        double speed = Math.hypot(dx, dz);
        double prevSpeed = Math.hypot(prevDX, prevDZ);

        if (speed > 0.1 && prevSpeed > 0.1 && !player.isOnGround()) {
            double dot = (dx * prevDX + dz * prevDZ) / (speed * prevSpeed);
            double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot))));
            if (angle > ANGLE_CHANGE_THRESHOLD) {
                handleViolation(data, player, 3.0, 12.0, 1.5);
            }
        }

        data.setLastDeltaX(dx);
        data.setLastDeltaZ(dz);
    }
}
